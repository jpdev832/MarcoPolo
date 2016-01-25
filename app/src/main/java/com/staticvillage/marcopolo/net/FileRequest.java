package com.staticvillage.marcopolo.net;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * Created by joelparrish.
 */
public class FileRequest extends Request<String> {
    private final static String TWO_DASHES = "--";
    private final static String CR_LF = "\r\n";
    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    protected static String generateContentType(final String boundary, final Charset charset) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("multipart/form-data; boundary=");
        buffer.append(boundary);

        if (charset != null) {
            buffer.append("; charset=");
            buffer.append(charset.name());
        }

        return buffer.toString();
    }

    protected static String generateBoundary() {
        final StringBuilder buffer = new StringBuilder();
        final Random rand = new Random();
        final int count = rand.nextInt(11) + 30; // a random size from 30 to 40

        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        return buffer.toString();
    }

    private String FILE_PART_NAME = "uploadFile";

    private final Response.Listener<String> mListener;
    private final File mFilePart;
    private final String mBoundary;
    private final String mContentType;

    public FileRequest(String url, File file, Response.Listener<String> listener,
                       Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        this.mListener = listener;
        this.mFilePart = file;
        this.mBoundary = generateBoundary();
        this.mContentType = generateContentType(mBoundary, null);
    }

    protected String getContentDeposition() {
        StringBuilder builder = new StringBuilder();
        builder.append("content-disposition: ")
                .append("form-data; ")
                .append("name=\"").append(FILE_PART_NAME).append("\"; ")
                .append("filename=\"").append(mFilePart.getName()).append("\"");
        return builder.toString();
    }

    protected String getContentType() {
        return "Content-Type: image/jpeg";
    }

    protected String getContentTranserEncoding() {
        return "Content-Transfer-Encoding: binary";
    }

    protected void writeFile(OutputStream out) throws IOException {
        final InputStream in = new FileInputStream(mFilePart);
        try {
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = in.read(tmp)) != -1) {
                out.write(tmp, 0, l);
            }
            out.flush();
        } finally {
            in.close();
        }
    }

    protected void writeTo(OutputStream out) throws IOException {
        out.write(TWO_DASHES.getBytes());
        out.write(mBoundary.getBytes());
        out.write(CR_LF.getBytes());

        out.write(getContentDeposition().getBytes());
        out.write(CR_LF.getBytes());
        out.write(getContentType().getBytes());
        out.write(CR_LF.getBytes());
        out.write(getContentTranserEncoding().getBytes());
        out.write(CR_LF.getBytes());

        out.write(CR_LF.getBytes());

        writeFile(out);
        out.write(CR_LF.getBytes());

        out.write(TWO_DASHES.getBytes());
        out.write(mBoundary.getBytes());
        out.write(TWO_DASHES.getBytes());
        out.write(CR_LF.getBytes());
    }

    @Override
    public String getBodyContentType() {
        return mContentType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            Log.d("marco_polo", "Network Response " + new String(response.data, "UTF-8"));
            return Response.success(new String(response.data, "UTF-8"), getCacheEntry());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.success(new String(response.data), getCacheEntry());
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
