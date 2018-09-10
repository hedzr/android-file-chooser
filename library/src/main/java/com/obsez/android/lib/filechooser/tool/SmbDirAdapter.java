package com.obsez.android.lib.filechooser.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.expeditors.gepha.expeditors.R;
import com.obsez.android.lib.filechooser.internals.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by coco on 6/9/18.
 */
public class SmbDirAdapter extends ArrayAdapter<SmbFile>{
    public SmbDirAdapter(Context cxt, List<SmbFile> entries, int resId) {
        super(cxt, resId, R.id.text1, entries);
        this.init(entries, null);
    }

    public SmbDirAdapter(Context cxt, List<SmbFile> entries, int resId, String dateFormat) {
        super(cxt, resId, R.id.text1, entries);
        this.init(entries, dateFormat);
    }

    public SmbDirAdapter(Context cxt, List<SmbFile> entries, int resource, int textViewResourceId) {
        super(cxt, resource, textViewResourceId, entries);
        this.init(entries, null);
    }

    @SuppressLint("SimpleDateFormat")
    private void init(List<SmbFile> entries, String dateFormat) {
        _formatter = new SimpleDateFormat(dateFormat != null && !"".equals(dateFormat.trim()) ? dateFormat.trim() : "yyyy/MM/dd HH:mm:ss");
        _entries = entries;
        _defaultFolderIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_folder);
        _defaultFileIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_file);
    }

    // This function is called to show each view item
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewGroup rl = (ViewGroup) super.getView(position, convertView, parent);

        TextView tvName = rl.findViewById(R.id.text1);
        TextView tvSize = rl.findViewById(R.id.txt_size);
        TextView tvDate = rl.findViewById(R.id.txt_date);
        //ImageView ivIcon = (ImageView) rl.findViewById(R.id.icon);

        tvDate.setVisibility(View.VISIBLE);

        Future<Void> thread = Executors.newSingleThreadExecutor().submit(() -> {
            SmbFile file = _entries.get(position);
            tvName.setText(file.getName());
            try{
                if (file.isDirectory()) {
                    final Drawable folderIcon = _defaultFolderIcon;
                    tvName.setCompoundDrawablesWithIntrinsicBounds(folderIcon, null, null, null);
                    tvSize.setText("");
                    if (!_entries.get(position).getName().trim().equals("..")) {
                        tvDate.setText(_formatter.format(new Date(file.lastModified())));
                    } else {
                        tvDate.setVisibility(View.GONE);
                    }
                } else {
                    final Drawable fileIcon = _defaultFileIcon;
                    tvName.setCompoundDrawablesWithIntrinsicBounds(fileIcon, null, null, null);
                    tvSize.setText(FileUtil.getReadableFileSize(file.length()));
                    tvDate.setText(_formatter.format(new Date(file.lastModified())));
                }
            } catch(SmbException e){
                e.printStackTrace();
            }

            return null;
        });

        try{
            thread.get();
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
     *
     * @deprecated no pint. can't get file icons on a samba server
     */
    @Deprecated
    public void setResolveFileType(boolean resolveFileType) {
        //noinspection deprecation
        this._resolveFileType = resolveFileType;
    }

    private static SimpleDateFormat _formatter;
    private List<SmbFile> _entries;
    private Drawable _defaultFolderIcon = null;
    private Drawable _defaultFileIcon = null;

    /**
     * @deprecated no pint. can't get file icons on a samba server
     */
    @Deprecated
    private boolean _resolveFileType = false;
}

