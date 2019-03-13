package tortel.fr.mapscanner.handler;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Messenger;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import tortel.fr.mapscanner.listener.IDataHandler;
import tortel.fr.mapscanner.listener.IPictureHandler;
import tortel.fr.mapscanner.task.ImageRequestTask;
import tortel.fr.mapscannerlib.ApiResponse;
import tortel.fr.mapscannerlib.MessageUtils;

public class PhotosHandler extends DataHandler implements IDataHandler, IPictureHandler {

    private Context context;

    public PhotosHandler(Messenger clientMessenger, Context context) {
        super(clientMessenger);
        this.context = context;
    }

    @Override
    public void onRequestSuccessful(JSONObject rawData) {
        ApiResponse response = trimPayload(rawData);
        ImageRequestTask task = new ImageRequestTask(this, context);

        try {
            JSONObject payload = new JSONObject(response.getPayload());
            int width = payload.getInt("width");
            int height = payload.getInt("height");

            String url = payload.getString("prefix") + width + "x" + height + payload.getString("suffix");

            task.execute(url);

        } catch (JSONException e) {
            Log.e("error", "Error to create the JSON object based on the payload");
        }
    }

    @Override
    public void onRequestFailed(JSONObject rawData) {
        ApiResponse response = trimError(rawData);
        sendToClient("photos", response, MessageUtils.PHOTOS_MSG);
    }

    @Override
    public void onPictureDownloaded(Bitmap bitmap) {
        ApiResponse response = new ApiResponse();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        response.setBitmap(byteArray);

        Log.d("paull", "image downloaded ! " + byteArray.length);
        sendToClient("photos", response, MessageUtils.PHOTOS_MSG);
    }

    @Override
    public void onPictureDownloadFailed(String error) {
        ApiResponse response = new ApiResponse();
        response.setErrorDetail("The photos has not been downloaded properly. An error has occured.");
        response.setCode(400);
        sendToClient("photos", response, MessageUtils.PHOTOS_MSG);
    }

    ApiResponse trimPayload(JSONObject rawData) {
        ApiResponse response = new ApiResponse();
        JSONObject payload = new JSONObject();

        try {
            JSONObject meta = rawData.getJSONObject("meta");
            response.setCode(meta.getInt("code"));
            response.setRequestId(meta.getString("requestId"));

            JSONObject resp = rawData.getJSONObject("response");
            JSONObject photos = resp.getJSONObject("photos");

            JSONArray photoArray = photos.getJSONArray("items");
            JSONObject item = photoArray.getJSONObject(0);
            payload.put("id", item.getString("id"));
            payload.put("prefix", item.getString("prefix"));
            payload.put("suffix", item.getString("suffix"));
            payload.put("width", item.getInt("width"));
            payload.put("height", item.getInt("height"));
            payload.put("tip", item.getJSONObject("tip"));

            response.setPayload(payload.toString());
        } catch (JSONException e) {
            Log.e("error", e.getMessage());
        }

        return response;
    }
}
