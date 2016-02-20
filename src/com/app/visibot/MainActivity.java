package com.app.visibot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.ApplicationConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends ActionBarActivity {
	EditText edsearch;
	ListView lvstaff;
	List<Staff> stafflist = null;
	List<Staff> stafflistfresh = null;
	ArrayList<Staff> arraylist;
	static String fetchurl = "";
	// Default maximum disk usage in bytes
	private static final int DEFAULT_DISK_USAGE_BYTES = 25 * 1024 * 1024;
	// Default cache folder name
	private static final String DEFAULT_CACHE_DIR = "photos";
	StaffViewAdapter adapter;
	ImageLoader.ImageCache imageCache;
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	ImageLoader imageLoader;
	String regId = "";
	GoogleCloudMessaging gcmObj;
	ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainscreen);
		configure();
		initialise();
		fetchstaff();
	}

	void fetchstaff() {
		String tag_json_obj = "json_obj_req";
		String url = "http://visibot.herokuapp.com/names";
		Cache cache = visibot.getInstance().getRequestQueue().getCache();
		stafflistfresh = new ArrayList<Staff>();

		Entry entry = cache.get(url);
		if (entry != null) {
			try {
				String data = new String(entry.data, "UTF-8");
				try {
					JSONArray array = new JSONArray(data);
					for (int i = 0; i < array.length(); i++) {
						try {
							Staff map = new Staff();
							JSONObject obj = (JSONObject) array.get(i);
							String name = obj.getString("name");
							Log.d("name", name);
							String imgurl = obj.getString("images");
							String id = obj.getString("id");

							map.setId(id);
							map.setRealname(name);
							map.setImgUrl(imgurl);
							stafflistfresh.add(map);

							// Log.d("stafflist",
							// stafflist.toString());

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					adapter = new StaffViewAdapter(MainActivity.this,
							stafflistfresh);
					edsearch.addTextChangedListener(new CustomTextWatcher());
					// Binds the Adapter to the ListView
					lvstaff.setAdapter(adapter);

				} catch (JSONException e) {
					// TODO Auto-generated catch
					// block
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if ((mobiledata() == NetworkInfo.State.CONNECTED)
				|| (mobiledata() == NetworkInfo.State.CONNECTING)
				|| (wifidata() == NetworkInfo.State.CONNECTED)
				|| (wifidata() == NetworkInfo.State.CONNECTING)) {

			StringRequest jsonObjReq = new StringRequest(Method.GET, url,
					new Response.Listener<String>() {

						@Override
						public void onResponse(String response) {
							Log.d("volleyresp", response.toString());
							try {
								JSONArray array = new JSONArray(response);
								for (int i = 0; i < array.length(); i++) {
									try {
										Staff map = new Staff();
										JSONObject obj = (JSONObject) array
												.get(i);
										String name = obj.getString("name");
										Log.d("name", name);
										String imgurl = obj.getString("images");
										String id = obj.getString("id");

										map.setId(id);
										map.setRealname(name);
										map.setImgUrl(imgurl);
										stafflistfresh.add(map);

										// Log.d("stafflist",
										// stafflist.toString());

									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								adapter = new StaffViewAdapter(
										MainActivity.this, stafflistfresh);

								// Binds the Adapter to the ListView
								lvstaff.setAdapter(adapter);

							} catch (JSONException e) {
								// TODO Auto-generated catch
								// block
								e.printStackTrace();
							}
						}
					}, new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							VolleyLog.d("volleyresperror",
									"Error: " + error.getMessage());
						}
					}) {

				@Override
				protected Response<String> parseNetworkResponse(
						NetworkResponse response) {
					String jsonString = new String(response.data);
					return Response.success(jsonString,
							HttpHeaderParser.parseIgnoreCacheHeaders(response));
				}

				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					HashMap<String, String> headers = new HashMap<String, String>();
					headers.put("session_id", "");
					return headers;
				}

			};
			visibot.getInstance().getRequestQueue().getCache()
					.invalidate(url, true);
			// Adding request to request queue
			visibot.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
		}

	}

	NetworkInfo.State mobiledata() {
		return ((ConnectivityManager) getSystemService("connectivity"))
				.getNetworkInfo(0).getState();
	}

	NetworkInfo.State wifidata() {
		return ((ConnectivityManager) getSystemService("connectivity"))
				.getNetworkInfo(1).getState();
	}

	void configure() {
		edsearch = (EditText) findViewById(R.id.edsearch);
		edsearch.addTextChangedListener(new CustomTextWatcher());
		lvstaff = (ListView) findViewById(R.id.lvsearch);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("Visibot");
		toolbar.setTitleTextColor(0xFFFFFFFF);
		toolbar.setNavigationIcon(null);
	}

	public class CustomTextWatcher implements TextWatcher,
			OnEditorActionListener {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		public void afterTextChanged(Editable h) {

			String text = edsearch.getText().toString()
					.toLowerCase(Locale.getDefault());
			adapter.filter(text);
			// String currenttext = edsearch.getText().toString().trim();
			// // if (stafflist != null) {
			// stafflist.clear();
			// if (currenttext.length() == 0) {
			// stafflist.addAll(arraylist);
			// } else {
			// for (Staff sup : arraylist) {
			// if (sup.getRealname().contains(currenttext)) {
			// stafflist.add(sup);
			// }
			// }
			// }
			// }

			// notifyDataSetChanged();
		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

			return false;
		}

	}

	private class StaffViewAdapter extends BaseAdapter {
		Context mContext;
		LayoutInflater inflater;
		protected int count;
		ProgressDialog progressDialog;
		String url;
		String filename;
		ProgressDialog mProgressDialog;
		BluetoothDevice device;

		public StaffViewAdapter(Context context, List<Staff> stafflistt) {
			mContext = context;
			stafflist = stafflistt;
			inflater = LayoutInflater.from(mContext);
			arraylist = new ArrayList<Staff>();
			arraylist.addAll(stafflist);
			imageCache = new BitmapLruCache();
			imageLoader = new ImageLoader(newRequestQueue(MainActivity.this),
					imageCache);
		}

		public void filter(String charText) {
			charText = charText.toLowerCase(Locale.getDefault());
			stafflist.clear();
			if (charText.length() == 0) {
				stafflist.addAll(arraylist);
			} else {
				for (Staff staff : arraylist) {
					if (staff.getRealname().toLowerCase(Locale.getDefault())
							.contains(charText)) {
						stafflist.add(staff);
					}
				}
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return stafflist.size();
		}

		@Override
		public Staff getItem(int position) {
			// TODO Auto-generated method stub
			return stafflist.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			final ViewHolder holder;
			if (view == null) {
				holder = new ViewHolder();
				view = inflater.inflate(R.layout.listview_item, null);
				// Locate the TextView in listview_item.xml
				holder.imgvdp = (NetworkImageView) view
						.findViewById(R.id.imgdp);
				holder.name = (TextView) view.findViewById(R.id.tvname);
				holder.id = (TextView) view.findViewById(R.id.tvid);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			holder.name.setText(stafflist.get(position).getRealname());
			holder.id.setText(stafflist.get(position).getId());
			holder.imgvdp.setImageUrl(stafflist.get(position).getImgUrl(),
					imageLoader);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					// Get the layout inflater
					LayoutInflater inflater = (MainActivity.this)
							.getLayoutInflater();
					builder.setTitle("Send Message");
					builder.setCancelable(true);
					builder.setIcon(R.drawable.slack);
					final View v = inflater.inflate(R.layout.dialogitem, null);
					builder.setView(v)
							.setPositiveButton("Send Message",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {
											String pid = holder.id.getText()
													.toString().trim();
											EditText edmsg = (EditText) v
													.findViewById(R.id.edvisitorname);
											String msg = edmsg.getText()
													.toString();
											if ((mobiledata() == NetworkInfo.State.CONNECTED)
													|| (mobiledata() == NetworkInfo.State.CONNECTING)
													|| (wifidata() == NetworkInfo.State.CONNECTED)
													|| (wifidata() == NetworkInfo.State.CONNECTING)) {
												sendmsg(pid, msg);
											} else {
												Toast.makeText(
														getApplicationContext(),
														"Enable internet first",
														Toast.LENGTH_SHORT)
														.show();
											}

										}
									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();
										}

									});
					builder.create();
					builder.show();
				}

			});
			return view;
		}

		public class ViewHolder {
			TextView name;
			TextView id;
			NetworkImageView imgvdp;

		}

		void sendmsg(String pid, String msg) {
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.add("user_id", pid);
			params.add("visitor_name", msg);

			client.post("http://visibot.herokuapp.com/send", params,
					new AsyncHttpResponseHandler() {

						@Override
						public void onStart() {
						}

						@Override
						public void onRetry(int retryNo) {
							// called when request is retried
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] response) {
							String s = new String(response);
							Log.d("200", s);
							if (statusCode == 200) {
								try {
									JSONObject obj = new JSONObject(s);
									String msg = obj.getString("message");
									Toast.makeText(getApplicationContext(),
											msg, Toast.LENGTH_SHORT).show();
								} catch (JSONException e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
								}
							}
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] errorResponse, Throwable e) {

						}
					});
		}

	}

	private static RequestQueue newRequestQueue(Context context) {
		// define cache folder
		File rootCache = context.getExternalCacheDir();
		if (rootCache == null) {
			Log.d("volley", "Can't find External Cache Dir, "
					+ "switching to application specific cache directory");
			rootCache = context.getCacheDir();
		}

		File cacheDir = new File(rootCache, DEFAULT_CACHE_DIR);
		cacheDir.mkdirs();

		HttpStack stack = new HurlStack();
		Network network = new BasicNetwork(stack);
		DiskBasedCache diskBasedCache = new DiskBasedCache(cacheDir,
				DEFAULT_DISK_USAGE_BYTES);
		RequestQueue queue = new RequestQueue(diskBasedCache, network);
		queue.start();

		return queue;
	}

	void initialise() {
		if (checkPlayServices()) {
			registerInBackground();
		} else {
			//register(false);
		}
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(MainActivity.this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode,
						MainActivity.this, PLAY_SERVICES_RESOLUTION_REQUEST)
						.show();
			} else {
				Log.i("Device", "This device is not supported.");
				// finish();
			}
			return false;
		}
		return true;
	}

	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				mProgressDialog = new ProgressDialog(MainActivity.this);
				mProgressDialog.setTitle("Creating new account");
				mProgressDialog.setMessage("Please wait...");
				mProgressDialog.setCancelable(false);
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setCanceledOnTouchOutside(false);
				mProgressDialog.show();
			}

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcmObj == null) {
						gcmObj = GoogleCloudMessaging
								.getInstance(MainActivity.this);
					}
					regId = gcmObj
							.register(ApplicationConstants.GOOGLE_PROJ_ID);
					msg = "Registration ID :" + regId;
					Log.d("regID", msg);

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				mProgressDialog.dismiss();
				// if (!TextUtils.isEmpty(regId)) {
				// AccountPrefs acc = new AccountPrefs(getActivity());
				// acc.putregid(regId);
				// getActivity().runOnUiThread(changeMessage);
				// register(true);
				// } else {
				// getActivity().runOnUiThread(changeMessage1);
				// register(true);
				// }
			}
		}.execute(null, null, null);
	}

	private Runnable changeMessage = new Runnable() {
		@Override
		public void run() {
			// Log.v(TAG, strCharacters);
			mProgressDialog
					.setMessage("Registered with GCM Server successfully.Finalizing...");
		}
	};
	private Runnable changeMessage1 = new Runnable() {
		@Override
		public void run() {
			// Log.v(TAG, strCharacters);
			mProgressDialog
					.setMessage("Sorry,Registration with GCM Server failed.Finalizing...");
		}
	};

}
