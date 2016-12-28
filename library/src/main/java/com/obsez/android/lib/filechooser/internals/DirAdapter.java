package com.obsez.android.lib.filechooser.internals;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.obsez.android.lib.filechooser.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by coco on 6/7/15.
 */
public class DirAdapter extends ArrayAdapter<File> {

    final static SimpleDateFormat _formatter = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );

    List<File> m_entries;

    public DirAdapter( Context cxt, List<File> entries, int resId ) {
        super( cxt, resId, R.id.text1, entries );
        m_entries = entries;
    }

    public DirAdapter( Context cxt, List<File> entries, int resource, int textViewResourceId ) {
        super( cxt, resource, textViewResourceId, entries );
        m_entries = entries;
    }

    // This function is called to show each view item
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        ViewGroup rl = (ViewGroup) super.getView( position, convertView, parent );
        if ( rl == null )
            return null;

        TextView tvName = (TextView) rl.findViewById( R.id.text1 );
        TextView tvSize = (TextView) rl.findViewById( R.id.txt_size );
        TextView tvDate = (TextView) rl.findViewById( R.id.txt_date );
        //ImageView ivIcon = (ImageView) rl.findViewById(R.id.icon);

        tvDate.setVisibility( View.VISIBLE );

        File file = m_entries.get( position );
        if ( file == null ) {
            tvName.setText( ".." );
            tvName.setCompoundDrawablesWithIntrinsicBounds( ContextCompat.getDrawable( getContext(), R.drawable.ic_folder ), null, null, null );
            tvDate.setVisibility( View.GONE );
        } else if ( file.isDirectory() ) {
            tvName.setText( m_entries.get( position ).getName() );
            tvName.setCompoundDrawablesWithIntrinsicBounds( ContextCompat.getDrawable( getContext(), R.drawable.ic_folder ), null, null, null );
            tvSize.setText( "" );
            if ( !m_entries.get( position ).getName().trim().equals( ".." ) ) {
                tvDate.setText( _formatter.format( new Date( file.lastModified() ) ) );
            } else {
                tvDate.setVisibility( View.GONE );
            }
        } else {
            tvName.setText( m_entries.get( position ).getName() );
            tvName.setCompoundDrawablesWithIntrinsicBounds( ContextCompat.getDrawable( getContext(), R.drawable.ic_file ), null, null, null );
            tvSize.setText( FileUtil.getReadableFileSize( file.length() ) );
            tvDate.setText( _formatter.format( new Date( file.lastModified() ) ) );
        }

        return rl;
    }
}

