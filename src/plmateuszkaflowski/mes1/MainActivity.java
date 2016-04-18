package plmateuszkaflowski.mes1;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

	float Rmin, Rmax, AlfaAir, TempBegin, C, Ro, K;
	Bitmap bitmap;
	int width;
	Canvas canvas;
	ImageView image;
	Grid grid;
	float x = 50, y = 50;
	Spinner spNh;
	BC bc;
	Solution solution;
	EditText etTauMax, etRMin, etRMax, etAlfaAir, etTempBegin, etTempPow, etK,
			etRo, etC;
	int taumax;
	Button blicz;

	void dataLoad(Grid grid, BC bc) {

		grid.Rmin = Float.parseFloat(etRMin.getText().toString());

		grid.Rmax = Float.parseFloat(etRMax.getText().toString());

		if (grid.Rmin >= grid.Rmax) {
			Toast.makeText(this, "RMin wiêksze od RMax!", Toast.LENGTH_LONG)
					.show();
			grid.Rmin = 0;
			etRMin.setText("0");
		} else if (grid.Rmin != 0) {
			Toast.makeText(this, "UWAGA: rysunek przedstawia œciankê rurki!",
					Toast.LENGTH_SHORT).show();
		}

		bc.AlfaPow = Float.parseFloat(etAlfaAir.getText().toString());

		bc.TempBegin = Float.parseFloat(etTempBegin.getText().toString());

		bc.TempPow = Float.parseFloat(etTempPow.getText().toString());

		taumax = Integer.parseInt(etTauMax.getText().toString());

		C = Float.parseFloat(etC.getText().toString());

		Ro = Float.parseFloat(etRo.getText().toString());

		K = Float.parseFloat(etK.getText().toString());

		Log.i("MES", "DANE WCZYTANE");

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		Display display = getWindowManager().getDefaultDisplay();
		width = display.getWidth(); // deprecated

		spNh = (Spinner) findViewById(R.id.spNh);
		addItemsOnSpinner();

		etTauMax = (EditText) findViewById(R.id.numTauMax);
		etRMin = (EditText) findViewById(R.id.etRMin);
		etRMax = (EditText) findViewById(R.id.etRMax);
		etAlfaAir = (EditText) findViewById(R.id.etAlfaAir);
		etTempBegin = (EditText) findViewById(R.id.etTempBegin);
		etTempPow = (EditText) findViewById(R.id.etTempPow);
		etK = (EditText) findViewById(R.id.etK);
		etRo = (EditText) findViewById(R.id.etRo);
		etC = (EditText) findViewById(R.id.etC);

		grid = new Grid(2, 2);
		bc = new BC();
		dataLoad(grid, bc);
		
		blicz = (Button) findViewById(R.id.button1);
		blicz.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				grid = new Grid(Integer.parseInt(spNh.getSelectedItem()
						.toString()), 2);
				dataLoad(grid, bc);
				solution = new Solution(grid, bc, C, Ro, K, taumax);
				Log.i("MES spinner nh=", spNh.getSelectedItem().toString());
				prepareImage();
			}
		});

		spNh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				grid = new Grid(Integer.parseInt(spNh.getSelectedItem()
						.toString()), 2);
				dataLoad(grid, bc);
				solution = new Solution(grid, bc, C, Ro, K, taumax);
				Log.i("MES spinner nh=", spNh.getSelectedItem().toString());
				prepareImage();
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		solution = new Solution(grid, bc, C, Ro, K, taumax);

		image = (ImageView) findViewById(R.id.image);

		createBitmap();
		canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, width, width, paint);
		image.setImageBitmap(bitmap);
		prepareImage();
		findViewById(R.id.spNh).requestFocus();

	}

	private void addItemsOnSpinner() {
		List<String> list = new ArrayList<String>();

		for (int i = 2; i < width / 2; i++) {
			if ((width / 2) % i == 0)
				list.add(Integer.toString(i));
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spNh.setAdapter(dataAdapter);
	}

	private void prepareImage() {
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, width, width, paint);

		// paint.setColor(Color.BLUE);

		float nhsize = (width / 2) / grid.nh;
		float r = width / 2;

		float min = grid.vrtxTemp[1];
		float max = grid.vrtxTemp[grid.nh];
		float scale = max - min;

		int index;
		float l = 0;

		if (grid.Rmin == 0) {
			l = (FloatMath.sqrt(Math.abs(width / 2 - x)
					* Math.abs(width / 2 - x) + Math.abs(width / 2 - y)
					* Math.abs(width / 2 - y)));
			index = (int) (l / nhsize);
		} else {
			index = (int) ((y - width / 2) / nhsize);
		}

		Paint paintcircle = new Paint();
		paintcircle.setTextSize(20);
		float hsv[] = { 0, 1, 1 };
		if (bc.TempPow < bc.TempBegin) // prêt cieplejszy od otoczenia
			hsv[0] = 170f;
		paintcircle.setColor(Color.HSVToColor(hsv));

		for (int i = grid.nh; i >= 1; i--) {
			hsv[2] = ((grid.vrtxTemp[i] - min) / scale);
			// Log.i("MES hsv2=", Float.toString(hsv[2]));
			Log.i("MES temp=", Float.toString(grid.vrtxTemp[i]));
			paintcircle.setColor(Color.HSVToColor(hsv));
			if (grid.Rmin == 0) { // prêt
				canvas.drawCircle(width / 2, width / 2, r, paintcircle);
				canvas.drawPoint(width / 2, width / 2 + r, paint);
				r -= nhsize;
			} else { // rurka
				canvas.drawRect(0, width / 2, width, width / 2 + r, paintcircle);
				canvas.drawPoint(width / 2, width / 2 + r, paint);
				r -= nhsize;
			}
		}

		if (grid.Rmin == 0) { // prêt
			if (x < width && y < width && l < width / 2) {
				canvas.drawText(
						"Temp. w punkcie = "
								+ Float.toString(grid.vrtxTemp[(index + 1)
										% (grid.nh + 1)]) + " [K]", 10, 20,
						paintcircle);
				canvas.drawCircle(x, y, 5, paint);
			}
		} else // rurka
		if (x < width && y < width && x > width / 2 && y > width / 2) {
			canvas.drawText(
					"Temp. w punkcie = "
							+ Float.toString(grid.vrtxTemp[index + 1]) + " [K]",
					10, 20, paintcircle);
			canvas.drawCircle(x, y, 5, paint);
		}

		paint.setColor(Color.MAGENTA);
		canvas.drawPoint(width / 2, width / 2, paint);
		image.setImageBitmap(bitmap);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_MOVE) {
			x = event.getX();
			y = event.getY();
			prepareImage();
			Log.i("MES X", Float.toString(x));
		}
		return true;
	}

	private void createBitmap() {
		bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
	}

}
