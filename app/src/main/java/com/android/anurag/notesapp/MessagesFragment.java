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

		/**public abstract boolean setViewValue (View view, Cursor cursor, int columnIndex)
		  * Binds the Cursor column defined by the specified index to the specified view. 
		  * When binding is handled by this ViewBinder, this method must return true. 
		  * If this method returns false, SimpleCursorAdapter will attempts to handle the binding on its own.
		*/
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

	/** To delete or to take any action on a particular message we need to find reference to the message in our database.
	 * 		This method finds ID of message and tasks can be performed on message accordingly.
	 */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.i(TAG, "item selected= " + l.getItemAtPosition(position).toString() + "and id= " + id + "....");
      //Toast.makeText(getActivity(), "item selected= " + l.getItemAtPosition(position).toString() + "and id= " + id + "....",Toast.LENGTH_LONG ).show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");
        Bundle args = new Bundle();
        Log.d(TAG, "onActivityCreated() and mListener.getProfileEmail()=" + mListener.getProfileEmail());
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
      //  getListView().smoothScrollToPosition(adapter.getCount() - 1); //this will scroll down smoothly
        ListView l= getListView();
       // l.setSelection(adapter.getCount() - 1);  //setSelection(position) will directly take to the position specified
        l.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        l.setStackFromBottom(true);
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

/*
    Compare getLastVisiblePosition() with getCount() in a Runnable
    to see if the entire ListView fits on the screen as soon as it has been drawn.
    You should also check to see if the last visible row fits entirely on the screen.

    Create the Runnable:

    ListView listView;
    Runnable fitsOnScreen = new Runnable() {
        @Override
        public void run() {
            int last = listView.getLastVisiblePosition();
            if(last == listView.getCount() - 1 && listView.getChildAt(last).getBottom() <= listView.getHeight()) {
                // It fits!
            }
            else {
                // It doesn't fit...
            }
        }
    };

    In onCreate() queue your Runnable in the ListView's Handler:
        listView.post(fitsOnScreen);
 */