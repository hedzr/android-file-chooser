package com.obsez.android.lib.smbfilechooser.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.obsez.android.lib.smbfilechooser.R;
import com.obsez.android.lib.smbfilechooser.SmbFileChooserDialog;
import com.obsez.android.lib.smbfilechooser.internals.FileUtil;
import com.obsez.android.lib.smbfilechooser.internals.UiUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static com.obsez.android.lib.smbfilechooser.SmbFileChooserDialog.getNetworkThread;

/**
 * Created by coco on 6/9/18. Edited by Guiorgy on 10/09/18.
 */
public class SmbDirAdapter extends ArrayAdapter<SmbFile>{
    public SmbDirAdapter(Context cxt, List<SmbFile> entries, int resId) {
        super(cxt, resId, R.id.text, entries);
        this.init(null);
    }

    public SmbDirAdapter(Context cxt, List<SmbFile> entries, int resId, String dateFormat) {
        super(cxt, resId, R.id.text, entries);
        this.init(dateFormat);
    }

    public SmbDirAdapter(Context cxt, List<SmbFile> entries, int resource, int textViewResourceId) {
        super(cxt, resource, textViewResourceId, entries);
        this.init(null);
    }

    @SuppressLint("SimpleDateFormat")
    private void init(String dateFormat) {
        _formatter = new SimpleDateFormat(dateFormat != null && !"".equals(dateFormat.trim()) ? dateFormat.trim() : "yyyy/MM/dd HH:mm:ss");
        _defaultFolderIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_folder);
        _defaultFileIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_file);

        int accentColor = UiUtil.getThemeAccentColor(getContext());
        int red = Color.red(accentColor);
        int green = Color.green(accentColor);
        int blue = Color.blue(accentColor);
        int accentColorWithAlpha = Color.argb(40, red, green, blue);
        _colorFilter = new PorterDuffColorFilter(accentColorWithAlpha, PorterDuff.Mode.MULTIPLY);
    }

    // This function is called to show each view item
    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        ViewGroup rl = (ViewGroup) super.getView(position, convertView, parent);

        final View root = rl.findViewById(R.id.root);
        final TextView tvName = rl.findViewById(R.id.text);
        final TextView tvSize = rl.findViewById(R.id.txt_size);
        final TextView tvDate = rl.findViewById(R.id.txt_date);
        //ImageView ivIcon = (ImageView) rl.findViewById(R.id.icon);

        tvDate.setVisibility(View.VISIBLE);

        Future ret = getNetworkThread().submit(new Runnable(){
            @Override
            public void run(){
                SmbFile file = SmbDirAdapter.super.getItem(position);
				if(file == null) return;
				String name = file.getName();
				name = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
                tvName.setText(name);
                try{
                    if(file.isDirectory()){
                        final Drawable folderIcon = _defaultFolderIcon;
                        tvName.setCompoundDrawablesWithIntrinsicBounds(folderIcon, null, null, null);
                        tvSize.setText("");
                        if(!file.getName().trim().equals("../") && !file.getName().trim().equals("..") && file.lastModified() != 0L){
                            tvDate.setText(_formatter.format(new Date(file.lastModified())));
                        } else{
                            tvDate.setVisibility(View.GONE);
                        }
                    } else{
                        final Drawable fileIcon = _defaultFileIcon;
                        tvName.setCompoundDrawablesWithIntrinsicBounds(fileIcon, null, null, null);
                        tvSize.setText(FileUtil.getReadableFileSize(file.length()));
                        if(file.lastModified() != 0L) tvDate.setText(_formatter.format(new Date(file.lastModified())));
                          else tvDate.setVisibility(View.GONE);
                    }

                    if(_selected.get(file.hashCode(), null) == null) root.getBackground().clearColorFilter();
                      else root.getBackground().setColorFilter(_colorFilter);
                } catch(SmbException e){
                    e.printStackTrace();
                }
            }
        });

        try{
            ret.get();
        } catch(InterruptedException | ExecutionException e){
            e.printStackTrace();
        }

        return rl;
    }

    public Drawable getDefaultFolderIcon() {
        return _defaultFolderIcon;
    }

    public void setDefaultFolderIcon(Drawable defaultFolderIcon) {
        this._defaultFolderIcon = defaultFolderIcon;
    }

    public Drawable getDefaultFileIcon() {
        return _defaultFileIcon;
    }

    public void setDefaultFileIcon(Drawable defaultFileIcon) {
        this._defaultFileIcon = defaultFileIcon;
    }

    /**
     * @deprecated no pint. can't get file icons on a samba server
     */
    @Deprecated
    public boolean isResolveFileType() {
        //noinspection deprecation
        return _resolveFileType;
    }

    /**
     * @deprecated no pint. can't get file icons on a samba server
     */
    @Deprecated
    public void setResolveFileType(boolean resolveFileType) {
        //noinspection deprecation
        this._resolveFileType = resolveFileType;
    }

    public void setEntries(List<SmbFile> entries){
        super.clear();
        super.addAll(entries);
        //notifyDataSetChanged();
    }

    @Override
    public long getItemId(final int position) {
        Future<Long> ret = getNetworkThread().submit(new Callable<Long>(){
            @Override
            public Long call(){
                //noinspection ConstantConditions
                return (long) getItem(position).hashCode();
            }
        });

        try{
            return ret.get();
        } catch(InterruptedException | ExecutionException e){
            e.printStackTrace();
        }

        return position;
    }

    public void selectItem(int position){
        int id = (int) getItemId(position);
        if(_selected.get(id, null) == null){
            _selected.append(id, getItem(position));
        } else{
            _selected.delete(id);
        }
        //notifyDataSetChanged();
    }

    public boolean isSelected(int position){
        return isSelectedById((int) getItemId(position));
    }

    public boolean isSelectedById(int id){
        return _selected.get(id, null) != null;
    }

    public boolean isAnySelected(){
        return _selected.size() > 0;
    }

    public boolean isOneSelected(){
        return  _selected.size() == 1;
    }

    public List<SmbFile> getSelected(){
        ArrayList<SmbFile> list = new ArrayList<SmbFile>();
        for(int i = 0; i < _selected.size(); i++){
            list.add(_selected.valueAt(i));
        }
        return list;
    }

    public void clearSelected(){
        _selected.clear();
    }

    private static SimpleDateFormat _formatter;
    private Drawable _defaultFolderIcon = null;
    private Drawable _defaultFileIcon = null;
    /**
     * @deprecated no point. can't get file icons on a samba server
     */
    @Deprecated
    private boolean _resolveFileType = false;
    private PorterDuffColorFilter _colorFilter;
    private SparseArray<SmbFile> _selected = new SparseArray<SmbFile>();
}

