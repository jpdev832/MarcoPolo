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

		Gson gson = new Gson();
		String json = gson.toJson(dataStruct);
		String expectedJson = "{\"name\":\"name\",\"data\":\"{\"data\":1234}\"};
		
        assertEquals(dataStruct.getName(), "name");
        assertEquals(dataStruct.getData(), "{\"data\":1234}");
		assertEquals(json, expectedJson);
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
        List<String> data = new LinkedList<>();
        data.add("hello");
        data.add("bye");

        PointMarker pointMarker = new PointMarker();
        pointMarker.setId(1234L);
        pointMarker.setTimestamp(569721600000L);
        pointMarker.setMarkerIndex(1234);
        pointMarker.setLatitude(40.573631);
        pointMarker.setLongitude(-73.976358);
		pointMarker.setData(data);
		pointMarker.setType("text");
		pointMarker.setRadius(50);
		
		Gson gson = new Gson();
		String json = gson.toJson(pointMarker);
		String expectedJson = "{}";
		
		assertEquals(pointMarker.getId(), 1234);
		assertEquals(pointMarker.getTimestamp(), 569721600000L);
		assertEquals(pointMarker.getMarkerIndex(), 1234);
		assertEquals(pointMarker.getLatitude(), 40.573631);
		assertEquals(pointMarker.getLongitude(), -73.976358);
		assertEquals(pointMarker.getData().size(), 2);
		assertEquals(pointMarker.getType(), "text");
		assertEquals(pointMarker.getRadius(), 50);
    }
	
	@Test
	public void responseTest() {
		Response response = new Response();
		response.setStatus("SUCCESS");
		response.setStatus_code(200);
		response.setMessage("saved");
		response.setPath("http://www.website.com/data/1234");
		
		Gson gson = new Gson();
		String json = gson.toJson(response);
		String expectedJson = "{\"status\":\"SUCCESS\",\"status_code\":200,\"message\":\"saved\",\"path\":\"http://www.website.com/data/1234\"}";
		
		assertEquals(response.getStatus(), "SUCCESS");
		assertEquals(response.getStatus_code(), 200);
		assertEquals(response.getMessage(), "saved");
		assertEquals(response.getPath(), "http://www.website.com/data/1234");
	}
}
