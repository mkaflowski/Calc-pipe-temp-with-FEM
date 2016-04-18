package plmateuszkaflowski.mes1;

import android.util.Log;

public class Grid {
	// pola
	int nh, ne, Np; // liczba wezlow, liczba elementow, liczba punktów
					// ca³kowania
	float vrtxTemp[];
	float vrtxCoordX[];
	float dR, Rmin, Rmax;

	// konstruktory:
	Grid() {
		nh = 51; 
		ne = nh - 1;
		Np = 2;
		vrtxTemp = new float[nh + 1];
		vrtxCoordX = new float[nh + 1];
	}

	Grid(int nh2, int Np2) {
		nh = nh2;
		ne = nh - 1;
		Np = Np2;
		vrtxTemp = new float[nh + 1];
		vrtxCoordX = new float[nh + 1];
	}

	// metody:
	void genGrid(BC bc) {
		dR = (Rmax - Rmin) / ne;
		Log.i("MES", "SIATKA WYGENEROWANA\ndR = " + Float.toString(dR));
		int i;
		float x = 0;
		for (i = 1; i <= nh; i++) {
			vrtxCoordX[i] = x; //wspó³rzêdne elementów
			vrtxTemp[i] = bc.TempBegin;//poczatkowa temperatura
			x = x + dR;
		}
	}

};
