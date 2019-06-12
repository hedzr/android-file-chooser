package com.obsez.android.lib.filechooser.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.obsez.android.lib.filechooser.MediaStorePicker;
import com.obsez.android.lib.filechooser.MediaType;
import com.obsez.android.lib.filechooser.demo.tool.ImageUtil;
import com.obsez.android.lib.filechooser.internals.FileUtil;
import com.obsez.android.lib.filechooser.tool.BitmapUtil;
import com.obsez.android.lib.filechooser.tool.RootFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChooseFileActivityFragment extends Fragment implements View.OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "ChooseFileActivityFragment";

    private CheckBox disableTitle;
    private CheckBox enableOptions;
    private CheckBox titleFollowsDir;
    private CheckBox enableMultiple;
    private CheckBox displayPath;
    private CheckBox dirOnly;
    private CheckBox allowHidden;
    private CheckBox continueFromLast;
    private CheckBox filterImages;
    private CheckBox displayIcon;
    private CheckBox dateFormat;
    private CheckBox customLayout;
    private CheckBox darkTheme;
    private CheckBox dpad;
    private CheckBox back;

    private String _path = null;
    private TextView _tv;
    private ImageView _iv;
    private ViewGroup _rootView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getActivity() != null;
        Context c = getActivity();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            String ext = FileUtil.getDefaultPath(c, true);
            String itl = FileUtil.getDefaultPath(c, false);
            Timber.v("ext: " + ext + ", total size: " + FileUtil.getReadableFileSize(
                FileUtil.readSDCard(c, true)) + ", free size: " + FileUtil.getReadableFileSize(
                FileUtil.readSDCard(c, true, true)));
            Timber.v("itl: " + itl + ", total size: " + FileUtil.getReadableFileSize(
                FileUtil.readSDCard(c, false)) + ", free size: " + FileUtil.getReadableFileSize(
                FileUtil.readSDCard(c, false, true)));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_choose_file, container, false);

        _rootView = (ViewGroup) root;
        _tv = root.findViewById(R.id.textView);
        _tv.setText(BuildConfig.VERSION_NAME);
        _iv = root.findViewById(R.id.imageView);

        disableTitle = root.findViewById(R.id.checkbox_disable_title);
        enableOptions = root.findViewById(R.id.checkbox_enable_options);
        titleFollowsDir = root.findViewById(R.id.checkbox_title_follow_dir);
        enableMultiple = root.findViewById(R.id.checkbox_enable_multiple);
        displayPath = root.findViewById(R.id.checkbox_display_path);
        dirOnly = root.findViewById(R.id.checkbox_dir_only);
        allowHidden = root.findViewById(R.id.checkbox_allow_hidden);
        continueFromLast = root.findViewById(R.id.checkbox_continue_from_last);
        filterImages = root.findViewById(R.id.checkbox_filter_images);
        displayIcon = root.findViewById(R.id.checkbox_display_icon);
        dateFormat = root.findViewById(R.id.checkbox_date_format);
        customLayout = root.findViewById(R.id.checkbox_custom_layout);
        darkTheme = root.findViewById(R.id.checkbox_dark_theme);
        dpad = root.findViewById(R.id.checkbox_dpad);
        back = root.findViewById(R.id.checkbox_back);

        titleFollowsDir.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) customLayout.setChecked(false);
        });

        displayPath.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) customLayout.setChecked(false);
        });

        dateFormat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) customLayout.setChecked(false);
        });

        customLayout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dateFormat.setChecked(false);
                darkTheme.setChecked(false);
                titleFollowsDir.setChecked(false);
                displayPath.setChecked(false);
            }
        });

        root.findViewById(R.id.btn_show_dialog).setOnClickListener(this);

        // since v1.16, we made this true by default.
        displayPath.setChecked(true);

        dpad.setChecked(true);


        // for MediaStorePicker

        root.findViewById(R.id.btn_images).setOnClickListener(this::onBtnImagesClick);
        root.findViewById(R.id.btn_videos).setOnClickListener(this::onBtnVideosClick);
        root.findViewById(R.id.btn_audios).setOnClickListener(this::onBtnAudiosClick);
        root.findViewById(R.id.btn_downloads).setOnClickListener(this::onBtnDownloadsClick);
        root.findViewById(R.id.btn_files).setOnClickListener(this::onBtnFilesClick);

        return root;
    }

    public void onBtnImagesClick(View v) {
        MediaStorePicker.Companion.get()
            .config(MediaType.IMAGES, true, R.id.fragment, (dlg, mediaType, bucket, position, bucketItem) -> {
                switch (mediaType) {
                    case IMAGES:
                    case VIDEOS: {
                        // for a match_parent [ImageView], getMeasuredWidth() return is right
                        int w = _iv.getMeasuredWidth(), h = 0;
                        boolean forceWidth = true;

                        if (bucketItem.getWidth() > 0 && bucketItem.getHeight() > 0) {
                            h = (int) ((float) w * bucketItem.getHeight() / bucketItem.getWidth());
                        }
                        Timber.d("onPicked: %d x %d, %d x %d, '%s', %s", w, h, bucketItem.getWidth(),
                            bucketItem.getHeight(), bucketItem.getPath(), bucketItem.getUri());

                        // and trying to retrieve a larger one
                        Bitmap bitmap = null;
                        if (mediaType == MediaType.IMAGES) {
                            bitmap = BitmapUtil.decodeBitmap(Objects.requireNonNull(getActivity()),
                                bucketItem.getUri(), w, h);
                        }
                        if (bitmap == null) {
                            bitmap = bucketItem.getThumbnail(Objects.requireNonNull(getActivity()), mediaType,
                                w, h, forceWidth);
                        }
                        if (bitmap != null) {
                            BitmapUtil.setBitmapTo(_iv, bitmap);
                        }

                        if (dlg != null) dlg.dismiss();
                        break;
                    }
                    default:
                        Toast.makeText(v.getContext(),
                            "onBucketItemClick($position, item: $item, bucket: $bucket)",
                            Toast.LENGTH_SHORT).show();
                }
                return null;
            }).show();
    }

    public void onBtnVideosClick(View v) {
        MediaStorePicker.Companion.get().config(MediaType.VIDEOS, true, R.id.fragment, null).show();
    }

    public void onBtnAudiosClick(View v) {
        MediaStorePicker.Companion.get().config(MediaType.AUDIOS, true, R.id.fragment, null).show();
    }

    public void onBtnDownloadsClick(View v) {
        MediaStorePicker.Companion.get().config(MediaType.DOWNLOADS, true, R.id.fragment, null).show();
    }

    public void onBtnFilesClick(View v) {
        MediaStorePicker.Companion.get().config(MediaType.FILES, true, R.id.fragment, null).show();
    }

    @Override
    public void onClick(View v) {
        //choose a file
        final Context ctx = this.getActivity();
        assert ctx != null;

        final ArrayList<File> files = new ArrayList<>();

        ChooserDialog chooserDialog;
        if (darkTheme.isChecked()) {
            chooserDialog = new ChooserDialog(ctx, R.style.FileChooserStyle_Dark);
        } else {
            chooserDialog = new ChooserDialog(ctx, R.style.FileChooserStyle);
        }

        chooserDialog
            .withResources(
                dirOnly.isChecked() ? R.string.title_choose_folder : R.string.title_choose_file,
                R.string.title_choose, R.string.dialog_cancel)
            .withOptionResources(R.string.option_create_folder, R.string.options_delete,
                R.string.new_folder_cancel, R.string.new_folder_ok)
            // Optionally, you can use Strings instead:
            /*.withStringResources(
            dirOnly.isChecked() ? "Choose a folder" : "Choose a file",
                "Choose", "Cancel")
            .withOptionStringResources("New folder",
                "Delete", "Cancel", "Ok")*/
            .disableTitle(disableTitle.isChecked())
            .enableOptions(enableOptions.isChecked())
            .titleFollowsDir(titleFollowsDir.isChecked())
            .displayPath(displayPath.isChecked())
            .enableDpad(dpad.isChecked());
        if (filterImages.isChecked()) {
            // Most common image file extensions (source: http://preservationtutorial.library.cornell
            // .edu/presentation/table7-1.html)
            chooserDialog.withFilter(dirOnly.isChecked(),
                allowHidden.isChecked(),
                "tif", "tiff", "gif", "jpeg", "jpg", "jif", "jfif",
                "jp2", "jpx", "j2k", "j2c", "fpx", "pcd", "png", "pdf");
        } else {
            chooserDialog.withFilter(dirOnly.isChecked(), allowHidden.isChecked());
        }
        if (enableMultiple.isChecked()) {
            chooserDialog.enableMultiple(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                chooserDialog
                    .withOnDismissListener(dialog -> {
                        if (files.isEmpty()) return;

                        ArrayList<String> paths = new ArrayList<>();
                        for (File file : files) {
                            paths.add(file.getAbsolutePath());
                        }

                        AlertDialog.Builder builder = darkTheme.isChecked() ? new AlertDialog.Builder(ctx,
                            R.style.FileChooserDialogStyle_Dark) : new AlertDialog.Builder(ctx,
                            R.style.FileChooserDialogStyle);
                        builder.setTitle(files.size() + " files selected:")
                            .setAdapter(new ArrayAdapter<>(ctx,
                                android.R.layout.simple_expandable_list_item_1, paths), null)
                            .create()
                            .show();
                    })
                    .withOnBackPressedListener(dialog -> {
                        files.clear();
                        dialog.dismiss();
                    })
                    .withOnLastBackPressedListener(dialog -> {
                        files.clear();
                        dialog.dismiss();
                    })
                    .withNegativeButtonListener((dialog, which) -> {
                        files.clear();
                        dialog.dismiss();
                    })
                    .withChosenListener((dir, dirFile) -> {
                        if (continueFromLast.isChecked()) {
                            _path = dir;
                        }
                        if (dirFile.isDirectory()) {
                            chooserDialog.dismiss();
                            return;
                        }
                        if (!files.remove(dirFile)) {
                            files.add(dirFile);
                        }
                    });
            } else {
                // OnDismissListener is not supported, so we simulate something similar anywhere where the
                // dialog might be dismissed.
                final Runnable onDismiss = () -> {
                    if (files.isEmpty()) return;

                    ArrayList<String> paths = new ArrayList<>();
                    for (File file : files) {
                        paths.add(file.getAbsolutePath());
                    }

                    AlertDialog.Builder builder = darkTheme.isChecked() ? new AlertDialog.Builder(ctx,
                        R.style.FileChooserDialogStyle_Dark) : new AlertDialog.Builder(ctx,
                        R.style.FileChooserDialogStyle);
                    builder.setTitle(files.size() + " files selected:")
                        .setAdapter(new ArrayAdapter<>(ctx,
                            android.R.layout.simple_expandable_list_item_1, paths), null)
                        .create()
                        .show();
                };

                chooserDialog
                    .withOnBackPressedListener(dialog -> {
                        files.clear();
                        dialog.dismiss();
                        onDismiss.run();
                    })
                    .withOnLastBackPressedListener(dialog -> {
                        files.clear();
                        dialog.dismiss();
                        onDismiss.run();
                    })
                    .withNegativeButtonListener((dialog, which) -> {
                        files.clear();
                        dialog.dismiss();
                        onDismiss.run();
                    })
                    .withChosenListener((dir, dirFile) -> {
                        if (continueFromLast.isChecked()) {
                            _path = dir;
                        }
                        if (dirFile.isDirectory()) {
                            chooserDialog.dismiss();
                            onDismiss.run();
                            return;
                        }
                        if (!files.remove(dirFile)) {
                            files.add(dirFile);
                        }
                    });
            }
        } else {
            chooserDialog.withChosenListener((dir, dirFile) -> {
                if (continueFromLast.isChecked()) {
                    _path = dir;
                }
                Toast.makeText(ctx, (dirFile.isDirectory() ? "FOLDER: " : "FILE: ") + dir,
                    Toast.LENGTH_SHORT).show();
                _tv.setText(dir);
                if (dirFile.isFile()) _iv.setImageBitmap(ImageUtil.decodeFile(dirFile));
            });
        }
        if (continueFromLast.isChecked() && _path != null) {
            chooserDialog.withStartFile(_path);
        }
        if (displayIcon.isChecked()) {
            chooserDialog.withIcon(R.mipmap.ic_launcher);
        }
        if (dateFormat.isChecked()) {
            chooserDialog.withDateFormat("dd MMMM yyyy");
        }
        if (customLayout.isChecked()) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy");
            PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(0x6033b5e5, PorterDuff.Mode.MULTIPLY);
            Drawable folderIcon = ContextCompat.getDrawable(ctx,
                com.obsez.android.lib.filechooser.R.drawable.ic_folder);
            Drawable fileIcon = ContextCompat.getDrawable(ctx,
                com.obsez.android.lib.filechooser.R.drawable.ic_file);
            final PorterDuffColorFilter filter = new PorterDuffColorFilter(0x600000aa,
                PorterDuff.Mode.SRC_ATOP);
            folderIcon.mutate().setColorFilter(filter);
            fileIcon.mutate().setColorFilter(filter);
            chooserDialog
                .withAdapterSetter(adapter ->
                    adapter.overrideGetView((file, isSelected, isFocused, convertView, parent, inflater) -> {
                        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.li_row, parent, false);

                        TextView tvName = view.findViewById(R.id.file_name);
                        TextView tvPath = view.findViewById(R.id.file_path);
                        TextView tvDate = view.findViewById(R.id.file_date);

                        tvDate.setVisibility(View.VISIBLE);
                        tvName.setText(file.getName());
                        Drawable icon;
                        if (file.isDirectory()) {
                            icon = folderIcon.getConstantState().newDrawable();
                            if (file.lastModified() != 0L) {
                                tvDate.setText(format.format(new Date(file.lastModified())));
                            } else {
                                tvDate.setVisibility(View.GONE);
                            }
                        } else {
                            icon = fileIcon.getConstantState().newDrawable();
                            tvDate.setText(format.format(new Date(file.lastModified())));
                        }
                        if (file.isHidden()) {
                            tvName.setText("HIDDEN");
                        }
                        tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);

                        if (!(file instanceof RootFile)) {
                            tvPath.setText(file.getPath());
                        } else {
                            tvPath.setText("");
                        }

                        View root = view.findViewById(R.id.root);
                        if (root.getBackground() == null) {
                            root.setBackgroundResource(android.R.color.holo_blue_light);
                        }
                        if (!isSelected) {
                            root.getBackground().clearColorFilter();
                        } else {
                            root.getBackground().setColorFilter(colorFilter);
                        }

                        return view;
                    }));
        }
        if (back.isChecked()) {
            chooserDialog.withOnBackPressedListener(dialog -> chooserDialog.goBack());
        }

        chooserDialog.show();
    }
}
