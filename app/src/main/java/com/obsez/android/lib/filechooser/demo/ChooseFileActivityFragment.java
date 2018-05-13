package com.obsez.android.lib.filechooser.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;


/**
 * A placeholder fragment containing a simple view.
 */
public class ChooseFileActivityFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ChooseFileActivityFragment";
    private String _path;
    private TextView _tv;
    private ImageView _iv;

    public ChooseFileActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_choose_file, container, false);
        _tv = (TextView) root.findViewById(R.id.textView);
        _iv = (ImageView) root.findViewById(R.id.imageView);
        root.findViewById(R.id.btn_choose_a_file).setOnClickListener(this);
        root.findViewById(R.id.btn_choose_a_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // choose a folder
                final Context ctx = getActivity();
                new ChooserDialog().with(ctx)
                        .withIcon(R.mipmap.ic_hedzr_logo)
                        .withFilter(true, false)
                        .withStartFile(_path)
                        .withDateFormat("HH:mm")
                        .withResources(R.string.title_choose_folder, R.string.title_choose, R.string.dialog_cancel)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                Toast.makeText(ctx, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                                _path = path;
                                _tv.setText(_path);
                            }
                        })
                        .build()
                        .show();
            }
        });
        return root;
    }

    @Override
    public void onClick(View v) {
        //choose a file
        final Context ctx = this.getActivity();
        new ChooserDialog().with(ctx)
                .withFilterRegex(false, false, ".*\\.(jpe?g|png)")
                .withStartFile(_path)
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        Toast.makeText(ctx, "FILE: " + path, Toast.LENGTH_SHORT).show();

                        _path = path;

                        _tv.setText(_path);

                        //_iv.setImageURI(Uri.fromFile(pathFile));

                        _iv.setImageBitmap(ImageUtil.decodeFile(pathFile));

                    }
                })
                .build()
                .show();
    }

}
