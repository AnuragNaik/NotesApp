package com.android.anurag.notesapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by anurag on 24/2/16.
 */
public class MessagesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private OnFragmentInteractionListener mListener;
    private SimpleCursorAdapter adapter;
    String TAG= "MessagesFragment";

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        Log.d(TAG, "onAttach()");
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
        Log.d(TAG, "onAttach() Finished...");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.chat_list_item,
                null,
                new String[]{DataProvider.COL_MSG, DataProvider.COL_AT},
                new int[]{R.id.text1, R.id.text2},
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                switch (view.getId()) {
                    case R.id.text1:
                        LinearLayout root = (LinearLayout) view.getParent().getParent();
                        if (cursor.getString(cursor.getColumnIndex(DataProvider.COL_FROM)) == null) {
                            Log.d(TAG, "setView Right..");
                            root.setGravity(Gravity.RIGHT);
                            root.setPadding(50, 10, 10, 10);
                        } else {
                            Log.d(TAG, "setView Left..");
                            root.setGravity(Gravity.LEFT);
                            root.setPadding(10, 10, 50, 10);
                        }
                        break;
                }
                return false;
            }
        });
        setListAdapter(adapter);
        Log.d(TAG, "onCreate() finished...");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.i(TAG, "item selected= " + l.getItemAtPosition(position).toString() + "and id= " + id + "....");
      //  Toast.makeText(getActivity(), "item selected= " + l.getItemAtPosition(position).toString() + "and id= " + id + "....",Toast.LENGTH_LONG ).show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");
        Bundle args = new Bundle();
        Log.d(TAG, "onActivityCreated() and mListener.getProfileEmail()="+mListener.getProfileEmail());
        args.putString(DataProvider.COL_EMAIL, mListener.getProfileEmail());
        getLoaderManager().initLoader(1, args, this);
        Log.d(TAG, "onActivityCreated() finish..");
        Log.d(TAG, args.getString(DataProvider.COL_EMAIL));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader(int id, Bundle args)");
        String eEmail = args.getString(DataProvider.COL_EMAIL);
        Log.d(TAG, "eEmail="+eEmail);
        CursorLoader loader = new CursorLoader(
                getActivity(),
                DataProvider.CONTENT_URI_MESSAGES,
                null,
                DataProvider.COL_TO + " = ? OR " + DataProvider.COL_FROM + " = ?",
                new String[]{ eEmail, eEmail},
                DataProvider.COL_AT + " ASC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished()");
        adapter.swapCursor(data);
        Log.d(TAG, "onLoadFinished() finish..");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoadReset()");
        adapter.swapCursor(null);
    }

    public interface OnFragmentInteractionListener {
        String getProfileEmail();
    }

}
