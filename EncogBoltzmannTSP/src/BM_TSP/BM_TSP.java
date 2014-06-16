package BM_TSP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.encog.Encog;
import org.encog.ml.data.specific.BiPolarNeuralData;
import org.encog.neural.thermal.BoltzmannMachine;

/**
 * A class for a Boltzmann machine that solves the TSP.
 * 
 * @author Bert
 * 
 */
public class BM_TSP {

	/**
	 * Runs the Boltzmann machine to solve the TSP.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// Request file name
//		Scanner in = new Scanner(System.in);
//		System.out.println("Enter filename:");
//		String infile = in.nextLine();
//		in.close();
//		System.out.println("The file you requested: "+infile);

		// Read requested file
		double[] xvals = new double[NCITY], yvals = new double[NCITY];
		
		// Hardcode file name
		String infile = "input.txt";
		BufferedReader br = new BufferedReader(new FileReader(infile));
		String line = br.readLine();
		int i = 0;
		while (line != null) {
			Matcher match = Pattern.compile("\\(([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)\\)").matcher(line.toString());
			while(match.find()) {
//			       System.out.println(match.group(1));    
			       xvals[i] = Double.parseDouble(match.group(1));
//			       System.out.println(match.group(2));    
			       yvals[i] = Double.parseDouble(match.group(2));
			       i++;
			}
			line = br.readLine();	
		}
		br.close();
		    
		double[][] distances = new double[NCITY][NCITY];
		
		for (int n = 0; n < xvals.length; n++) {
			System.out.println(xvals[n]+", "+yvals[n]);
		}
		
		for (int j = 0; j < distances.length; j++) {
			for (int k = 0; k < distances.length; k++) {
				if (j == k) {
					distances[j][k] = 0;
				} else {
					distances[j][k] = Math.sqrt((xvals[j]-xvals[k])*(xvals[j]-xvals[k]) + (yvals[j]-yvals[k])*(yvals[j]-yvals[k]));
					System.out.println("The distance from node "+(j+1)+" to node "+(k+1)+" is "+distances[j][k]);
				}
			}
		}
		
		// Solve TSP
		BM_TSP program = new BM_TSP();
		program.run(2, 0.99, distances);
		Encog.getInstance().shutdown();
	}

	/**
	 * Sets-up the Boltzmann machine in the Encog framework.
	 * 
	 * @param temp
	 *            the temperature (for SA)
	 */
	public void run(int temp, double cooling, double[][] distances) {
		BoltzmannMachine boltzTSPsolver = new BoltzmannMachine(NNEURON);
		buildMap(distances); // create representation of cities
		getWeights(boltzTSPsolver); // set weights
		boltzTSPsolver.setTemperature(temp);

		do {
			boltzTSPsolver.establishEquilibrium();
			System.out.println("Temp: " + boltzTSPsolver.getTemperature()
					+ "\n" + listPath(boltzTSPsolver.getCurrentState()) + "\n");
			// Print paths
			boltzTSPsolver.decreaseTemperature(cooling); // cooling factor
		} while (!isValidPath(boltzTSPsolver.getCurrentState()));

		System.out.println("Temp: " + boltzTSPsolver.getTemperature()
				+ "\n" + listPath(boltzTSPsolver.getCurrentState()) + "\n");
		
		System.out.println("Path Length: "
				+ this.pathLength(boltzTSPsolver.getCurrentState()) * 100);
	}

	public static final int NCITY = 10; //number of cities
	public static final int NNEURON = NCITY * NCITY; // 100 neuron square network
	private double[][] dist; // distance matrix

	/**
	 * Fills the distance matrix with distance values.
	 * @param distances the distance matrix for TSP
	 */
	public void buildMap(double[][] distances) {
		this.dist = distances;
	}

	/**
	 * Determines the path length for a configuration of the BM.
	 * 
	 * @param dat
	 *            the Boltzmann machine states
	 * @return the path length
	 */
	public double pathLength(BiPolarNeuralData dat) {
		double out = 0; // start at 0
		int a, b, c;
		for (a = 0; a < NCITY; a++) { // iterate through all positions (5)
										// horizontal
			for (b = 0; b < NCITY; b++) { // iterate through all positions (5)
											// vertical
				if (dat.getBoolean(((a) % NCITY) * NCITY + b))
					break;
			}
			for (c = 0; c < NCITY; c++) {
				if (dat.getBoolean(((a + 1) % NCITY) * NCITY + c))
					break;
			}
			out += dist[b][c]; // only add valid connections
		}
		return out;
	}

	/**
	 * Checks to see if a path is valid.
	 * 
	 * @param dat
	 *            the Boltzmann machine states
	 * @return true if the tour is valid, false otherwise
	 */
	public boolean isValidPath(BiPolarNeuralData dat) {
		for (int a = 0; a < NCITY; a++) {
			int cities = 0;
			int stops = 0;
			for (int b = 0; b < NCITY; b++) {
				if (dat.getBoolean(b + a * NCITY)) {
					if (++cities > 1) { // two cities simultaneously
						return false;
					}
				}
				if (dat.getBoolean(a + b * NCITY)) {
					if (++stops > 1) { // same place twice
						return false;
					}
				}
			}
			if ((stops != 1) || (cities != 1)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Displays the path for a given Boltzmann machine configuration.
	 * 
	 * @param dat
	 *            the Boltzmann machine states
	 * @return a string representation of the path
	 */
	String listPath(BiPolarNeuralData dat) {
		int a, b;
		boolean firstP;
		StringBuilder out = new StringBuilder();
		for (a = 0; a < NCITY; a++) {
			firstP = true;
			out.append("[");
			for (b = 0; b < NCITY; b++) {
				if (dat.getBoolean(a * NCITY + b)) {
					if (firstP) {
						firstP = false;
						out.append(b);
					} else {
						out.append(", " + b);
					}
				}
			}
			out.append("]");
			// Arrow for all except last city
			if (a != NCITY - 1) {
				out.append("=>");
			}
		}

		return out.toString();
	}

	private double pen = 1; // constant for penalizing inappropriate paths

	/**
	 * Sets up the weights such that visiting the same place twice and being in
	 * several places simultaneously is penalized.
	 * 
	 * @param weights
	 *            the Boltzmann machine weights
	 */
	public void getWeights(BoltzmannMachine weights) {
		int fromIdx, toIdx = 0, predToPath = 0, succToPath = 0;
		double weight;
		for (int fromPath = 0; fromPath < NCITY; fromPath++) {
			for (int fromCity = 0; fromCity < NCITY; fromCity++) {
				fromIdx = fromCity + fromPath * NCITY;
				for (int toPath = 0; toPath < NCITY; toPath++) {
					for (int toCity = 0; toCity < NCITY; toCity++) {
						// //DEBUG
						// System.out.println("fIdx: "+fIdx);
						// System.out.println("toIdx: "+toIdx);
						// System.out.println("predToPath: "+predToPath);
						// System.out.println("succToPath: "+succToPath);

						toIdx = toCity + toPath * NCITY;
						weight = 0;
						if (fromIdx != toIdx) {
							predToPath = (toPath == 0 ? NCITY - 1 : toPath - 1);
							succToPath = (toPath == NCITY - 1 ? 0 : toPath + 1);
							// penalize circular paths
							if ((fromCity == toCity) || (fromPath == toPath)) {
								weight = -pen;
							}
							// penalize same city
							else if ((fromPath == succToPath)
									|| (fromPath == predToPath)) {
								weight = -dist[fromCity][toCity];
							}
						}
						weights.setWeight(fromIdx, toIdx, weight); // set the
																	// weights
																	// in Encog
					}
				}
				weights.getThreshold()[fromIdx] = -0.5 * pen;
			}
		}
	}
}
