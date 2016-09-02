package com.sam_chordas.android.stockhawk.ui;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class DetailFragment extends Fragment {

    static final String STOCK_SYMBOL = "SYMBOL";

    public DetailFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String symbol;
        View view;

        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }

        view = inflater.inflate(R.layout.fragment_detail, container, false);

        symbol = arguments.getString(DetailFragment.STOCK_SYMBOL);

        Log.d("APP", symbol);

        Cursor c = getContext().getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                null,
                QuoteColumns.SYMBOL + " = ? ",
                new String[]{symbol},
                null);
        if (c == null) {
            return null;
        }

        c.moveToFirst();

        getActivity().setTitle(c.getString(c.getColumnIndex(QuoteColumns.NAME)));

        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        return view;
    }

}
