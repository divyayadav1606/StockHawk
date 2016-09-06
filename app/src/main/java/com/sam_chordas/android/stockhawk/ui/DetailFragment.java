package com.sam_chordas.android.stockhawk.ui;


import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteHistoricalColumn;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_QUOTE = 1;
    private static final int CURSOR_LOADER_HISTORICAL_DATA = 2;
    static final String STOCK_SYMBOL = "SYMBOL";
    String symbol;

    GraphView graph;

    @BindView(R.id.open) TextView open;
    @BindView(R.id.mkt_cap) TextView mkt_cap;
    @BindView(R.id.day_high) TextView day_high;
    @BindView(R.id.year_high) TextView year_high;
    @BindView(R.id.day_low) TextView day_low;
    @BindView(R.id.year_low) TextView year_low;
    @BindView(R.id.volume) TextView volume;
    @BindView(R.id.avg_vol) TextView avg_volume;
    @BindView(R.id.yield) TextView yield;
    @BindView(R.id.pneratio) TextView pneratio;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(CURSOR_LOADER_QUOTE, null, this);
        getLoaderManager().initLoader(CURSOR_LOADER_HISTORICAL_DATA, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;

        Bundle arguments = getArguments();

        if (arguments == null) {
            return null;
        }

        view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        symbol = arguments.getString(DetailFragment.STOCK_SYMBOL);

        //Get Stock Historical Data
        getHistoricalData task = new getHistoricalData();
        task.execute(symbol);

        graph = (GraphView) view.findViewById(R.id.graph);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CURSOR_LOADER_QUOTE:
                return new CursorLoader(getContext(), QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns._ID,
                                QuoteColumns.SYMBOL,
                                QuoteColumns.OPEN,
                                QuoteColumns.MKTCAP,
                                QuoteColumns.DAYSHIGH,
                                QuoteColumns.YEARHIGH,
                                QuoteColumns.DAYSLOW,
                                QuoteColumns.YEARLOW,
                                QuoteColumns.VOLUME,
                                QuoteColumns.AVGVOLUME,
                                QuoteColumns.YIELD,
                                QuoteColumns.PEGRATIO,
                                QuoteColumns.NAME },
                                QuoteColumns.SYMBOL + " = \"" + symbol + "\"", null, null);
            case CURSOR_LOADER_HISTORICAL_DATA:
                return new CursorLoader(getContext(), QuoteProvider.HistoricalData.CONTENT_URI,
                        new String[]{
                                QuoteHistoricalColumn._ID,
                                QuoteHistoricalColumn.SYMBOL,
                                QuoteHistoricalColumn.DATE,
                                QuoteHistoricalColumn.PRICE},
                        QuoteHistoricalColumn.SYMBOL + " = \"" + symbol + "\"",
                        null, null);
                default:
                    throw new IllegalStateException();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (!cursor.moveToFirst()) {
            return;
        }

        switch(loader.getId()) {
            case CURSOR_LOADER_QUOTE: {
                Log.d("Load", String.valueOf(cursor.getCount()));
                Log.d("Load", String.valueOf(cursor.getColumnCount()));
                open.setText(String.valueOf(cursor.getString(2)));
                mkt_cap.setText(String.valueOf(cursor.getString(3)));
                day_high.setText(String.valueOf(cursor.getString(4)));
                year_high.setText(String.valueOf(cursor.getString(5)));
                day_low.setText(String.valueOf(cursor.getString(6)));
                year_low.setText(String.valueOf(cursor.getString(7)));
                volume.setText(String.valueOf(cursor.getString(8)));
                avg_volume.setText(String.valueOf(cursor.getString(9)));
                yield.setText(String.valueOf(cursor.getString(10)));
                pneratio.setText(String.valueOf(cursor.getString(11)));
            }
                break;

            case CURSOR_LOADER_HISTORICAL_DATA:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Format to get Historical data for a symobol
     * http://query.yahooapis.com/v1/public/yql?q=
     * select * from   yahoo.finance.historicaldata
     * where  symbol    = "SYMBOL"
     * and    startDate = "yyyy-mm-dd"
     * and    endDate   = "yyy-mm-dd"
     * &format=json
     * &diagnostics=true
     * &env=store://datatables.org/alltableswithkeys
     * &callback=
     */

    private class getHistoricalData extends AsyncTask<String, Void, String> {

        public getHistoricalData() {
        }

        protected void onPostExecute(String str) {
            if (str == null)
                return;

            JSONObject jsonObject = null;
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
            try {
                jsonObject = new JSONObject(str);
                jsonObject = jsonObject.getJSONObject("query");
                JSONArray history = jsonObject.getJSONObject("results").getJSONArray("quote");

                for (int i = 0; i < history.length(); i++ ) {
                    Log.d("APP", String.valueOf(history.getJSONObject(i).getString("Open")));
                    //series.appendData(new DataPoint(i, y));
                }

                graph.addSeries(series);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                // Load historic stock data
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date currentDate = new Date();

                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(currentDate);
                calEnd.add(Calendar.DATE, 0);

                Calendar calStart = Calendar.getInstance();
                calStart.setTime(currentDate);
                calStart.add(Calendar.YEAR, -1);

                String startDate = dateFormat.format(calStart.getTime());
                String endDate = dateFormat.format(calEnd.getTime());

                String query = "http://query.yahooapis.com/v1/public/yql?q= " + "select * from yahoo.finance.historicaldata where symbol=\"" +
                        symbol + "\" and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"" +
                        "&format=json%20&diagnostics=true%20&env=store://datatables.org/alltableswithkeys%20&callback=";

                Log.d("Date", query);

                URL url = new URL("http://query.yahooapis.com/v1/public/yql?q=%20select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol=%22GOOG%22%20and%20startDate=%222015-09-04%22%20and%20endDate=%222016-09-04%22&format=json%20&diagnostics=true%20&env=store://datatables.org/alltableswithkeys%20&callback=");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                return buffer.toString();
            } catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException ignored) {
                    }
                }
            }
        }
    }
}
