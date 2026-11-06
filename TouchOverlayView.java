package com.openclaw.game;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
import org.json.*;
import java.io.*;
import java.util.*;

/**
 * Transparent, multi-touch game overlay.
 * Coordinates and size are normalized against the current view dimensions.
 * Put any PNG in Assets/ControlsCustom and reference its filename in touch_config.json.
 */
public class TouchOverlayView extends View {
    public interface ActionSink {
        void onAction(String action, boolean pressed, float x, float y);
    }
    private static final class Control {
        String id, image, action, mode;
        float x, y, size;
        Bitmap bitmap;
        RectF bounds = new RectF();
    }
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final ArrayList<Control> controls = new ArrayList<>();
    private final HashMap<Integer, Control> activePointers = new HashMap<>();
    private ActionSink sink;
    private boolean editMode = false;
    private Control dragging;
    private float dragDx, dragDy;
    private String assetFolder = "ControlsCustom";
    private float density;

    public TouchOverlayView(Context c) { super(c); init(); }
    public TouchOverlayView(Context c, AttributeSet a) { super(c,a); init(); }
    public TouchOverlayView(Context c, AttributeSet a, int s) { super(c,a,s); init(); }
    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setClickable(true);
        density = getResources().getDisplayMetrics().density;
    }
    public void setActionSink(ActionSink s) { sink=s; }
    public void setEditMode(boolean enabled) {
        editMode=enabled; activePointers.clear(); dragging=null; invalidate();
    }
    public boolean isEditMode() { return editMode; }

    public void loadFromAssets(AssetManager assets) throws IOException, JSONException {
        controls.clear();
        String config = readText(assets, assetFolder + "/touch_config.json");
        JSONObject root = new JSONObject(config);
        JSONArray list = root.getJSONArray("buttons");
        for (int i=0;i<list.length();i++) {
            JSONObject o=list.getJSONObject(i);
            Control c=new Control();
            c.id=o.optString("id","control_"+i);
            c.image=o.getString("image");
            c.action=o.getString("action").toUpperCase(Locale.US);
            c.mode=o.optString("mode","button");
            c.x=clamp((float)o.optDouble("x",0.5),0,1);
            c.y=clamp((float)o.optDouble("y",0.5),0,1);
            c.size=clamp((float)o.optDouble("size",0.15),0.05f,0.6f);
            try (InputStream in=assets.open(assetFolder+"/"+c.image)) {
                c.bitmap=BitmapFactory.decodeStream(in);
            }
            if(c.bitmap==null) throw new IOException("Invalid PNG: "+c.image);
            controls.add(c);
        }
        requestLayout(); invalidate();
    }
    /** Optional: load directly from app's internal/external files directory. */
    public void loadFromDirectory(File directory) throws IOException, JSONException {
        controls.clear();
        File cfg=new File(directory,"touch_config.json");
        String config=readFile(cfg);
        JSONArray list=new JSONObject(config).getJSONArray("buttons");
        for(int i=0;i<list.length();i++){
            JSONObject o=list.getJSONObject(i); Control c=new Control();
            c.id=o.optString("id","control_"+i); c.image=o.getString("image");
            c.action=o.getString("action").toUpperCase(Locale.US);
            c.mode=o.optString("mode","button");
            c.x=clamp((float)o.optDouble("x",.5),0,1);
            c.y=clamp((float)o.optDouble("y",.5),0,1);
            c.size=clamp((float)o.optDouble("size",.15),.05f,.6f);
            c.bitmap=BitmapFactory.decodeFile(new File(directory,c.image).getAbsolutePath());
            if(c.bitmap==null) throw new IOException("Cannot decode "+c.image);
            controls.add(c);
        }
        invalidate();
    }
    public void saveConfig(File file) throws IOException {
        JSONObject root=new JSONObject(); JSONArray arr=new JSONArray();
        try {
            root.put("version",1); root.put("coordinateSystem","normalized_0_1_center");
            for(Control c:controls){
                JSONObject o=new JSONObject(); o.put("id",c.id); o.put("image",c.image);
                o.put("x",c.x); o.put("y",c.y); o.put("size",c.size);
                o.put("action",c.action); o.put("mode",c.mode); arr.put(o);
            }
            root.put("buttons",arr);
            try(FileOutputStream out=new FileOutputStream(file)){
                out.write(root.toString(2).getBytes("UTF-8"));
            }
        } catch(JSONException e){ throw new IOException(e); }
    }
    private static String readText(AssetManager a,String name)throws IOException{
        try(InputStream in=a.open(name); ByteArrayOutputStream b=new ByteArrayOutputStream()){
            byte[] buf=new byte[4096]; int n; while((n=in.read(buf))>0)b.write(buf,0,n);
            return b.toString("UTF-8");
        }
    }
    private static String readFile(File f)throws IOException{
        try(InputStream in=new FileInputStream(f); ByteArrayOutputStream b=new ByteArrayOutputStream()){
            byte[] buf=new byte[4096]; int n; while((n=in.read(buf))>0)b.write(buf,0,n);
            return b.toString("UTF-8");
        }
    }
    private static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    private void layoutControls(){
        float unit=Math.min(getWidth(),getHeight());
        for(Control c:controls){
            float d=c.size*unit;
            float cx=c.x*getWidth(), cy=c.y*getHeight();
            c.bounds.set(cx-d/2,cy-d/2,cx+d/2,cy+d/2);
        }
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas); layoutControls();
        for(Control c:controls){
            if(c.bitmap!=null){
                paint.setAlpha(editMode?210:190);
                canvas.drawBitmap(c.bitmap,null,c.bounds,paint);
            }
            if(editMode){
                paint.setAlpha(220); paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2*density); paint.setColor(Color.CYAN);
                canvas.drawRoundRect(c.bounds,8*density,8*density,paint);
                paint.setStyle(Paint.Style.FILL); paint.setColor(Color.WHITE);
                paint.setTextSize(11*density);
                canvas.drawText(c.id+" · "+c.action,c.bounds.left,c.bounds.top-5*density,paint);
            }
        }
        paint.setAlpha(255);
    }
    private Control hit(float x,float y){
        for(int i=controls.size()-1;i>=0;i--) if(controls.get(i).bounds.contains(x,y)) return controls.get(i);
        return null;
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        int index=e.getActionIndex(), id=e.getPointerId(index);
        float x=e.getX(index), y=e.getY(index);
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:{
                if(editMode){
                    dragging=hit(x,y);
                    if(dragging!=null){dragDx=x-dragging.x*getWidth();dragDy=y-dragging.y*getHeight();return true;}
                    return true;
                }
                Control c=hit(x,y); if(c==null)return false;
                activePointers.put(id,c);
                dispatch(c,true,x,y);
                invalidate(); return true;
            }
            case MotionEvent.ACTION_MOVE:{
                if(editMode && dragging!=null){
                    dragging.x=clamp((x-dragDx)/Math.max(1,getWidth()),0,1);
                    dragging.y=clamp((y-dragDy)/Math.max(1,getHeight()),0,1);
                    invalidate(); return true;
                }
                for(int i=0;i<e.getPointerCount();i++){
                    int pid=e.getPointerId(i); Control c=activePointers.get(pid);
                    if(c!=null) dispatch(c,true,e.getX(i),e.getY(i));
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:{
                if(editMode){dragging=null;return true;}
                Control c=activePointers.remove(id);
                if(c!=null){dispatch(c,false,x,y);invalidate();return true;}
                return true;
            }
        }
        return true;
    }
    private void dispatch(Control c,boolean down,float px,float py){
        if(sink==null)return;
        float vx=0,vy=0;
        if("joystick".equals(c.mode)){
            float cx=c.x*getWidth(),cy=c.y*getHeight(),r=Math.max(1,c.size*Math.min(getWidth(),getHeight())*.5f);
            vx=clamp((px-cx)/r,-1,1); vy=clamp((py-cy)/r,-1,1);
        }
        sink.onAction(c.action,down,vx,vy);
    }
}
