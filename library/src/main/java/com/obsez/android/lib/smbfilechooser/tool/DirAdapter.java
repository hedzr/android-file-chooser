package com.obsez.android.lib.smbfilechooser.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.obsez.android.lib.smbfilechooser.R;
import com.obsez.android.lib.smbfilechooser.internals.FileUtil;
import com.obsez.android.lib.smbfilechooser.internals.UiUtil;
import com.obsez.android.lib.smbfilechooser.internals.WrappedDrawable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by coco on 6/7/15.
 */
public class DirAdapter extends ArrayAdapter<File> {

    public DirAdapter(Context cxt, List<File> entries, int resId) {
        super(cxt, resId, R.id.text, entries);
        this.init(cxt, null);
    }

    public DirAdapter(Context cxt, List<File> entries, int resId, String dateFormat) {
        super(cxt, resId, R.id.text, entries);
        this.init(cxt, dateFormat);
    }

    public DirAdapter(Context cxt, List<File> entries, int resource, int textViewResourceId) {
        super(cxt, resource, textViewResourceId, entries);
        this.init(cxt, null);
    }

    @SuppressLint("SimpleDateFormat")
    private void init(Context context, String dateFormat) {
        _formatter = new SimpleDateFormat(dateFormat != null && !"".equals(dateFormat.trim()) ? dateFormat.trim() : "yyyy/MM/dd HH:mm:ss");
        _defaultFolderIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_folder);
        _defaultFileIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_file);

        int accentColor = context.getResources().getColor(android.support.v7.appcompat.R.color.material_deep_teal_500);
        int red = Color.red(accentColor);
        int green = Color.green(accentColor);
        int blue = Color.blue(accentColor);
        int accentColorWithAlpha = Color.argb(40, red, green, blue);
        _colorFilter = new PorterDuffColorFilter(accentColorWithAlpha, PorterDuff.Mode.MULTIPLY);
    }

    // This function is called to show each view item
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewGroup rl = (ViewGroup) super.getView(position, convertView, parent);

        TextView tvName = rl.findViewById(R.id.text);
        TextView tvSize = rl.findViewById(R.id.txt_size);
        TextView tvDate = rl.findViewById(R.id.txt_date);
        //ImageView ivIcon = (ImageView) rl.findViewById(R.id.icon);

        tvDate.setVisibility(View.VISIBLE);

        File file = super.getItem(position);
		if(file == null) return rl;
        tvName.setText(file.getName());
        if (file.isDirectory()) {
            final Drawable folderIcon = _defaultFolderIcon;
            tvName.setCompoundDrawablesWithIntrinsicBounds(folderIcon, null, null, null);
            tvSize.setText("");
            if (!file.getName().trim().equals("../") && !file.getName().trim().equals("..") && file.lastModified() != 0L) {
                tvDate.setText(_formatter.format(new Date(file.lastModified())));
            } else {
                tvDate.setVisibility(View.GONE);
            }
        } else {
            Drawable d = null;
            if (_resolveFileType) {
                d = UiUtil.resolveFileTypeIcon(getContext(), Uri.fromFile(file));
                if (d != null) {
                    d = new WrappedDrawable(d, 24, 24);
                }
            }
            if (d == null) {
                d = _defaultFileIcon;
            }
            tvName.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
            tvSize.setText(FileUtil.getReadableFileSize(file.length()));
            if(file.lastModified() != 0L) tvDate.setText(_formatter.format(new Date(file.lastModified())));
              else tvDate.setVisibility(View.GONE);
        }

        View root = rl.findViewById(R.id.root);
        if(_selected.get(file.hashCode(), null) == null) root.getBackground().clearColorFilter();
          else root.getBackground().setColorFilter(_colorFilter);
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

    public boolean isResolveFileType() {
        return _resolveFileType;
    }

    public void setResolveFileType(boolean resolveFileType) {
        this._resolveFileType = resolveFileType;
    }

	public void setEntries(List<File> entries){
		super.clear();
        super.addAll(entries);
        notifyDataSetChanged();
	}

    @Override
    public long getItemId(int position) {
        //noinspection ConstantConditions
        return getItem(position).hashCode();
    }

    public void selectItem(int position){
        int id = (int) getItemId(position);
        if(_selected.get(id, null) == null){
            _selected.append(id, getItem(position));
        } else{
            _selected.delete(id);
        }
        notifyDataSetChanged();
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

    public List<File> getSelected(){
        ArrayList<File> list = new ArrayList<File>();
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
    private boolean _resolveFileType = false;
    private PorterDuffColorFilter _colorFilter;
    private SparseArray<File> _selected = new SparseArray<File>();
}

