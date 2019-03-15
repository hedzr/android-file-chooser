package com.obsez.android.lib.filechooser.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.obsez.android.lib.filechooser.internals.FileUtil;

import java.io.File;
import java.util.ArrayList;

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
    private CheckBox darkTheme;

    private String _path = null;
    private TextView _tv;
    private ImageView _iv;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getActivity() != null;
        String ext = FileUtil.getStoragePath(getActivity(), true);
        String itl = FileUtil.getStoragePath(getActivity(), false);
        Timber.v("ext: " + ext + ", total size: " + FileUtil.getReadableFileSize(
            FileUtil.readSDCard(getActivity(), true)) + ", free size: " + FileUtil.getReadableFileSize(
            FileUtil.readSDCard(getActivity(), true, true)));
        Timber.v("itl: " + itl + ", total size: " + FileUtil.getReadableFileSize(
            FileUtil.readSDCard(getActivity(), false)) + ", free size: " + FileUtil.getReadableFileSize(
            FileUtil.readSDCard(getActivity(), false, true)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_choose_file, container, false);

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
        darkTheme = root.findViewById(R.id.checkbox_dark_theme);

        root.findViewById(R.id.btn_show_dialog).setOnClickListener(this);

        // since v1.16, we made this true by default.
        displayPath.setChecked(true);

        return root;
    }

    @Override
    public void onClick(View v) {
        //choose a file
        final Context ctx = this.getActivity();
        assert ctx != null;

        final ArrayList<File> files = new ArrayList<>();

        ChooserDialog chooserDialog;
        if (darkTheme.isChecked())
            chooserDialog = new ChooserDialog(ctx, R.style.FileChooserStyle_Dark);
        else chooserDialog = new ChooserDialog(ctx);

        chooserDialog.withResources(dirOnly.isChecked() ? R.string.title_choose_folder : R.string.title_choose_file,
            R.string.title_choose, R.string.dialog_cancel)
            .withOptionResources(R.string.option_create_folder, R.string.options_delete,
                R.string.new_folder_cancel, R.string.new_folder_ok)
            .disableTitle(disableTitle.isChecked())
            .enableOptions(enableOptions.isChecked())
            .titleFollowsDir(titleFollowsDir.isChecked())
            .displayPath(displayPath.isChecked());
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

                        new AlertDialog.Builder(ctx)
                            .setTitle(files.size() + " files selected:")
                            .setAdapter(new ArrayAdapter<>(ctx,
                                android.R.layout.simple_expandable_list_item_1, paths), null)
                            .create()
                            .show();
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

                    new AlertDialog.Builder(ctx)
                        .setTitle(files.size() + " files selected:")
                        .setAdapter(new ArrayAdapter<>(ctx,
                            android.R.layout.simple_expandable_list_item_1, paths), null)
                        .create()
                        .show();
                };

                chooserDialog
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

        chooserDialog.build().show();
    }
}
