package plmateuszkaflowski.mes1;

import android.util.Log;

class Solution {

	float TauMax;

	Solution(Grid grid, BC bc, float C, float Ro, float K, int taumax) {
		float dTau, Tau, a, Alfa;
		float E[], W[], N1[], N2[], r[], H[][], P[], TempTau[];
		E = new float[3];
		W = new float[3];
		N1 = new float[3];
		N2 = new float[3];
		r = new float[3];
		H = new float[3][3];
		P = new float[3];
		TempTau = new float[3];
		float Rp, TpTau;
		float nTime;
		int i, ie, ip, iTime;

		float aC[], aD[], aE[], aB[], X[];

		a = K / (C * Ro);

		W[1] = 1; // waga punktow calkowania
		W[2] = 1;
		E[1] = -0.5773502692f; // wspolrzedne lokalne punktow calkowania
		E[2] = 0.5773502692f;
		N1[1] = 0.5f * (1 - E[1]); // funkcje ksztaltu w punkcie calkowania
		N1[2] = 0.5f * (1 - E[2]);
		N2[1] = 0.5f * (1 + E[1]);
		N2[2] = 0.5f * (1 + E[2]);

		// generacja siatki MES
		grid.genGrid(bc);

		dTau = (grid.dR * grid.dR) / (0.5f * a); // krok czasowy
		Log.i("MES", "dTau = " + Float.toString(dTau));

		TauMax = taumax; // czas

		nTime = (TauMax / dTau) + 1; // iloœæ krokow czasowych
		dTau = TauMax / nTime;

		aC = new float[grid.nh + 1];
		aD = new float[grid.nh + 1];
		aE = new float[grid.nh + 1];
		aB = new float[grid.nh + 1]; //
		X = new float[grid.nh + 1]; // wektor niewiadomych

		Tau = 0;

		for (iTime = 1; iTime <= nTime; iTime++) // glowna petla po czasie

		{

			for (int t = 0; t <= grid.nh; t++) {
				aC[t] = 0;
				aD[t] = 0;
				aE[t] = 0;
				aB[t] = 0;
			}

			for (ie = 1; ie <= grid.ne; ie++)// petla po elementach
			{
				r[1] = grid.vrtxCoordX[ie]; // odczytanie wspó³rzêdnych
				r[2] = grid.vrtxCoordX[ie + 1];// dla wêz³ów w elemencie
				TempTau[1] = grid.vrtxTemp[ie];// odczytanie temperatur
				TempTau[2] = grid.vrtxTemp[ie + 1];
				grid.dR = r[2] - r[1]; // odleg³oœæ wêz³ów

				Alfa = 0;
				if (ie == grid.ne)
					Alfa = bc.AlfaPow;
				// wymiana ciep³a z otoczeniem odbywa siê dla ostatniego
				// elementu

				for (int j = 0; j <= 2; j++) {
					P[j] = 0;// zerowanie
					for (int k = 0; k <= 2; k++) {
						H[j][k] = 0;
					}

				}

				for (ip = 1; ip <= grid.Np; ip++) {
					Rp = N1[ip] * r[1] + N2[ip] * r[2];
					TpTau = N1[ip] * TempTau[1] + N2[ip] * TempTau[2];
					// liczenie macierzy lokalnej
					H[1][1] = H[1][1] + K * Rp * W[ip] / grid.dR + C * Ro
							* grid.dR * Rp * W[ip] * N1[ip] * N1[ip] / dTau;
					H[1][2] = H[1][2] - K * Rp * W[ip] / grid.dR + C * Ro
							* grid.dR * Rp * W[ip] * N1[ip] * N2[ip] / dTau;
					H[2][1] = H[1][2];
					H[2][2] = H[2][2] + K * Rp * W[ip] / grid.dR + C * Ro
							* grid.dR * Rp * W[ip] * N2[ip] * N2[ip] / dTau + 2
							* Alfa * grid.Rmax;
					P[1] = P[1] + C * Ro * grid.dR * TpTau * Rp * W[ip]
							* N1[ip] / dTau;
					P[2] = P[2] + C * Ro * grid.dR * TpTau * Rp * W[ip]
							* N2[ip] / dTau + 2 * Alfa * grid.Rmax * bc.TempPow;
					/*--*/
				}

				aD[ie] = aD[ie] + H[1][1];
				aD[ie + 1] = aD[ie + 1] + H[2][2];
				aE[ie] = aE[ie] + H[1][2];
				aC[ie + 1] = aC[ie + 1] + H[2][1];
				aB[ie] = aB[ie] + P[1];
				aB[ie + 1] = aB[ie + 1] + P[2];
			}

			Gaus(grid.nh, aC, aD, aE, aB, X);
			// wywo³anie podprogramu do rozwi¹zania równania metod¹ Gaussa

			for (i = 1; i <= grid.nh; i++)
				grid.vrtxTemp[i] = X[i];

			Tau = Tau + dTau;
		}

		Log.i("MES", "KONIEC OBLICZEN");

	}

	void Gaus(int nh, float aC[], float aD[], float aE[], float aB[], float x[]) {

		float C[][] = new float[nh + 1][nh + 1];

		float M[] = new float[nh + 1];

		for (int i = 0; i <= nh; i++) {
			for (int j = 0; j <= nh; j++)
				C[i][j] = 0; // zerujemy tablice
			M[i] = 0;
		}

		// poni¿ej wype³niam macierz C, równanie ma postaæ C*X=M
		for (int i = 1; i <= nh; i++)
			C[i][i] = aD[i]; // main diagonal

		for (int i = 1; i < nh; i++) {
			C[i + 1][i] = aC[i + 1];// przek¹tna pod g³ówn¹ przek¹tn¹
			C[i][i + 1] = aE[i]; // przek¹tna nad g³ówn¹ przek¹tn¹
		}

		for (int i = 1; i <= nh; i++)
			M[i] = aB[i]; // prawa strona rownania

		int NB, J, l, JJ, i;
		float xm, suma;

		NB = nh - 1;

		// rozwi¹zywanie uk³adu równañ
		for (J = 1; J <= NB; J++) {

			l = J + 1;
			for (JJ = l; JJ <= l; JJ++) {
				xm = C[JJ][J] / C[J][J];
				for (i = J; i <= nh; i++)
					C[JJ][i] = C[JJ][i] - C[J][i] * xm;

				M[JJ] = M[JJ] - (M[J] * xm);

			}
		}

		x[nh] = M[nh] / C[nh][nh];
		for (J = 1; J <= NB; J++) {
			JJ = nh - J;
			l = JJ + 1;
			suma = 0.0f;
			for (i = l; i <= nh; i++)
				suma = suma + C[JJ][i] * x[i];

			x[JJ] = (M[JJ] - suma) / C[JJ][JJ];
		}
	}
};
