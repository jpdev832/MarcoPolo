package com.staticvillage.marcopolo;

import com.staticvillage.marcopolo.model.DataStruct;
import com.staticvillage.marcopolo.model.MarkerData;
import com.staticvillage.marcopolo.model.PointMarker;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by joelparrish on 1/25/16.
 */
public class ModelTest {
    @Test
    public void dataStructTest() {
        DataStruct<String> dataStruct = new DataStruct<>("name", "{\"data\":1234}");

        assertEquals(dataStruct.getName(), "name");
        assertEquals(dataStruct.getData(), "{\"data\":1234}");
    }

    @Test
    public void markerDataTest() {
        MarkerData markerData = new MarkerData();
        markerData.setPointMarkerId(1234);
        markerData.setData("hello");

        assertEquals(markerData.getPointMarkerId(), 1234);
        assertEquals(markerData.getData(), "hello");
    }

    @Test
    public void pointMarkerTest() {
        MarkerData markerData1 = new MarkerData();
        MarkerData markerData2 = new MarkerData();

        markerData1.setPointMarkerId(1234);
        markerData1.setData("hello");
        markerData2.setPointMarkerId(1234);
        markerData2.setData("bye");

        List<String> data = new LinkedList<>();
        data.add("hello");
        data.add("bye");

        PointMarker pointMarker = new PointMarker();
        pointMarker.setId(1234L);
        pointMarker.setTimestamp(569721600000L);
        pointMarker.setMarkerIndex(1234);
        pointMarker.setLatitude(40.573631);
        pointMarker.setLongitude(-73.976358);

    }
}
