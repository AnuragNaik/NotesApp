package com.android.anurag.notesapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by anurag on 24/2/16.
 */
public class MessagesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>   {

    private DbHelper dbHelper;
    private static SQLiteDatabase db;
    private OnFragmentInteractionListener mListener;
    private SimpleCursorAdapter adapter;
    private ListView listView;
    private ListAdapter listAdapter;
    static String TAG= "MessagesFragment";
    private DbQueries dbQueries;

    public SimpleCursorAdapter getAdapter() {
        return adapter;
    }

    public DbQueries getDbQueries() {
        return dbQueries;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbQueries = new DbQueries(getActivity().getApplicationContext());
        Log.d(TAG, "onCreate()");
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.chat_list_item,
                null,
                new String[]{DataProvider.COL_MSG},
                new int[]{R.id.text1},
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
                        LinearLayout messageBox = (LinearLayout) view.getParent();
                        TextView statusText=(TextView) messageBox.findViewById(R.id.message_status);
                        TextView timeTextView = (TextView) messageBox.findViewById(R.id.text2);
                        if(cursor.getString(cursor.getColumnIndex(DataProvider.COL_READ))!=null){
                            statusText.setText("Read");
                        }
                        else if(cursor.getString(cursor.getColumnIndex(DataProvider.COL_DELIVERED))!=null){
                            statusText.setText("Delivered");
                        }
                        else if(cursor.getString(cursor.getColumnIndex(DataProvider.COL_SENT))!=null){
                           statusText.setText("Sent");
                        }
                        else{
                            statusText.setText("Pending");
                        }

                        if (cursor.getString(cursor.getColumnIndex(DataProvider.COL_FROM)) == null) {
                            Log.d(TAG, "setView Right..");
                            root.setGravity(Gravity.RIGHT);
                            root.setPadding(50, 10, 10, 10);
                            messageBox.setBackgroundResource(R.drawable.box);
                        } else {
                            Log.d(TAG, "setView Left..");
                            root.setGravity(Gravity.LEFT);
                            root.setPadding(10, 10, 50, 10);
                            messageBox.setBackgroundResource(R.drawable.boxleft);
                            statusText.setText("");
                        }
                        String time = DateTimeUtils.get12HourFormatTime(cursor.getString(cursor.getColumnIndex(DataProvider.COL_AT)));
                        timeTextView.setText(time);
                        break;

                }
                return false;
            }
        });
        setListAdapter(adapter);
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
        args.putString(DataProvider.COL_USER_ID, mListener.getProfileEmail());
        getLoaderManager().initLoader(1, args, this);
        Log.d(TAG, "onActivityCreated() finish..");
        Log.d(TAG, args.getString(DataProvider.COL_USER_ID));

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final List list = new ArrayList();
        listView = this.getListView();
        listAdapter= getListAdapter();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                list.clear();
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.chat_context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                // Capture total checked items
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
                if(checked){
                    list.add(id);
                }
                else{
                    if(list.contains(id)) {
                      list.remove(id);
                    }
                }

            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_id:
                        listView.getSelectedItem();
                        Toast.makeText(getActivity(), "delete Clicked", Toast.LENGTH_SHORT).show();
                        for(int i=0; i<list.size(); i++){
                            dbQueries.deleteMessageByID((list.get(i)).toString());
                        }
                        list.clear();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                for(int i=0; i<list.size(); i++){
                    Log.d(TAG, "id: "+list.get(i));
                }
                list.clear();
            }
        });
    }

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
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach()");
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader(int id, Bundle args)");
        String eEmail = args.getString(DataProvider.COL_USER_ID);
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
       // l.setSelection(adapter.getCount() - 1);  //setSelection(position) will directly take to the position specified
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setStackFromBottom(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoadReset()");
        adapter.swapCursor(null);
    }

    public interface OnFragmentInteractionListener {
        String getProfileEmail();
    }

    public void scrollToBottomInChatList(){
        listView.setSelection(adapter.getCount()-1);
    }

    public void setAsSentInMessageTable(String messageId){
        Log.i(TAG, "updating data ");
/*
        ContentValues values = new ContentValues(2);
        values.put(DataProvider.COL_MSG, "msg");
        values.put(DataProvider.COL_FROM, "anurag@gmail.com");
        values.put(DataProvider.COL_TO, "");
        getActivity().getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
  */      Log.i(TAG, "updating data ");

        ContentValues dataToInsert = new ContentValues(1);
        String timeStamp = new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Calendar.getInstance().getTime());
        dataToInsert.put(DataProvider.COL_SENT, timeStamp);
        String where= "_id=?";
        String[] whereArgs=new String[] {String.valueOf(messageId)};
        getActivity().getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES, messageId), dataToInsert, null, null);
        Log.i(TAG, "data updated");
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