package com.obsez.android.lib.filechooser.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
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

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.obsez.android.lib.filechooser.R;
import com.obsez.android.lib.filechooser.internals.FileUtil;
import com.obsez.android.lib.filechooser.internals.UiUtil;
import com.obsez.android.lib.filechooser.internals.WrappedDrawable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by coco on 6/7/15.
 */
public class DirAdapter extends ArrayAdapter<File> {

    public DirAdapter(Context cxt, String dateFormat) {
        super(cxt, R.layout.li_row_textview, R.id.text, new ArrayList<>());
        this.init(dateFormat);
    }

    public DirAdapter(Context cxt, List<File> entries, int resId, String dateFormat) {
        super(cxt, resId, R.id.text, entries);
        this.init(dateFormat);
    }

    @SuppressLint("SimpleDateFormat")
    private void init(String dateFormat) {
        _formatter = new SimpleDateFormat(
            dateFormat != null && !"".equals(dateFormat.trim()) ? dateFormat.trim() : "yyyy/MM/dd HH:mm:ss");
        _defaultFolderIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_folder);
        _defaultFileIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_file);

        TypedArray ta = getContext().obtainStyledAttributes(R.styleable.FileChooser);
        int colorFilter = ta.getColor(R.styleable.FileChooser_fileListItemSelectedTint, getContext().getResources().getColor(R.color.li_row_background_tint));
        ta.recycle();
        _colorFilter = new PorterDuffColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY);
    }

    @FunctionalInterface
    public interface GetView {
        /**
         * @param file file that should me displayed
         * @param isSelected whether file is selected when _enableMultiple is set to true
         * @param isFocused whether this file is focused when using dpad controls
         * @param convertView see {@link ArrayAdapter#getView(int, View, ViewGroup)}
         * @param parent see {@link ArrayAdapter#getView(int, View, ViewGroup)}
         * @return your custom row item view
         */
        @NonNull
        View getView(@NonNull File file, boolean isSelected, boolean isFocused, View convertView, @NonNull ViewGroup parent);
    }

    public void overrideGetView(GetView getView) {
        this._getView = getView;
    }

    // This function is called to show each view item
    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final File file = super.getItem(position);
        if (file == null) return super.getView(position, convertView, parent);
        final boolean isSelected = _selected.get(file.hashCode(), null) != null;
        if (_getView != null)
            return _getView.getView(file, isSelected, position == _hoveredIndex, convertView, parent);

        ViewGroup view = (ViewGroup) super.getView(position, convertView, parent);

        TextView tvName = view.findViewById(R.id.text);
        TextView tvSize = view.findViewById(R.id.txt_size);
        TextView tvDate = view.findViewById(R.id.txt_date);
        //ImageView ivIcon = (ImageView) view.findViewById(R.id.icon);

        tvDate.setVisibility(View.VISIBLE);
        tvName.setText(file.getName());
        Drawable icon;
        if (file.isDirectory()) {
            icon = _defaultFolderIcon.getConstantState().newDrawable();
            tvSize.setText("");
            if (file.lastModified() != 0L) {
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
            icon = d.getConstantState().newDrawable();
            tvSize.setText(FileUtil.getReadableFileSize(file.length()));
            tvDate.setText(_formatter.format(new Date(file.lastModified())));
        }
        if (file.isHidden()) {
            final PorterDuffColorFilter filter = new PorterDuffColorFilter(0x80ffffff,
                PorterDuff.Mode.SRC_ATOP);
            icon.mutate().setColorFilter(filter);
        }
        tvName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        View root = view.findViewById(R.id.root);
        if (root.getBackground() == null) {
            root.setBackgroundResource(R.color.li_row_background);
        }
        if (!isSelected) {
            if (position == _hoveredIndex) {
                root.getBackground().setColorFilter(_colorFilter);
            } else {
                root.getBackground().clearColorFilter();
            }
        } else {
            root.getBackground().setColorFilter(_colorFilter);
        }

        return view;
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

    public void setEntries(List<File> entries) {
        setNotifyOnChange(false);
        super.clear();
        setNotifyOnChange(true);
        super.addAll(entries);
        //_hoveredIndex = -1;
    }

    @Override
    public long getItemId(int position) {
        try {
            //noinspection ConstantConditions
            return getItem(position).hashCode();
        } catch (IndexOutOfBoundsException e) {
            try {
                //noinspection ConstantConditions
                return getItem(0).hashCode();
            } catch (IndexOutOfBoundsException ex) {
                return 0;
            }
        }
    }

    public void selectItem(int position) {
        int id = (int) getItemId(position);
        if (_selected.get(id, null) == null) {
            _selected.append(id, getItem(position));
        } else {
            _selected.delete(id);
        }
        notifyDataSetChanged();
    }

    public boolean isSelected(int position) {
        return isSelectedById((int) getItemId(position));
    }

    public boolean isSelectedById(int id) {
        return _selected.get(id, null) != null;
    }

    public boolean isAnySelected() {
        return _selected.size() > 0;
    }

    public boolean isOneSelected() {
        return _selected.size() == 1;
    }

    public List<File> getSelected() {
        ArrayList<File> list = new ArrayList<File>();
        for (int i = 0; i < _selected.size(); i++) {
            list.add(_selected.valueAt(i));
        }
        return list;
    }

    public void clearSelected() {
        _selected.clear();
    }

    public int getHoveredIndex() {
        return _hoveredIndex;
    }

    public void setHoveredIndex(int i) {
        _hoveredIndex = i;
    }

    public int increaseHoveredIndex() {
        _hoveredIndex++;
        if (_hoveredIndex >= super.getCount()) _hoveredIndex = super.getCount() - 1;
        notifyDataSetInvalidated();
        return _hoveredIndex;
    }

    public int decreaseHoveredIndex() {
        _hoveredIndex--;
        if (_hoveredIndex < 0) _hoveredIndex = 0;
        notifyDataSetInvalidated();
        return _hoveredIndex;
    }

    public int push() {
        _indexStack.add(_hoveredIndex);
        return _hoveredIndex;
    }

    public int push(int index) {
        _indexStack.add(index);
        _hoveredIndex = index;
        return index;
    }

    public int pop() {
        if (!_indexStack.isEmpty()) {
            int x = _indexStack.get(_indexStack.size() - 1);
            _indexStack.remove(_indexStack.size() - 1);
            _hoveredIndex = x;
            return x;
        }
        return -1;
    }

    public void popAll() {
        _indexStack.clear();
    }

    private SimpleDateFormat _formatter;
    private Drawable _defaultFolderIcon = null;
    private Drawable _defaultFileIcon = null;
    private boolean _resolveFileType = false;
    private PorterDuffColorFilter _colorFilter;
    private SparseArray<File> _selected = new SparseArray<File>();
    private int _hoveredIndex;
    private List<Integer> _indexStack = new LinkedList<>();
    private GetView _getView = null;
}

