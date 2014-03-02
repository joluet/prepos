/*
 * Copyright (c) 2014 Jonas Luethke
 */

package de.tuhh.luethke.PrePos.Transformation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.PrePos.utility.Measurement;

public class DelayEmbedder {

	/**
	 * Maximum time difference between two data points: If time difference is
	 * smaller the data points are considered to be conditionally dependent.
	 * */
	private static final long MAX_TIME_DIFF_SEC = 1830; // 30.5 minutes
	private static final long MIN_TIME_DIFF_SEC = 1770; //  29.5 minutes
	
	//private static final double MIN_TRAVEL_DIST = 7000d;

	/**
	 * This method does the actual data transformation. Positional time series
	 * data is mapped to n-dimensional space to encode conditional dependency.
	 * 
	 * @param data
	 * @param order
	 * @return
	 */
	/*public static LinkedList<SimpleMatrix> transformTSData(List<Measurement> data, int order) {
		LinkedList<SimpleMatrix> transformedData = new LinkedList<SimpleMatrix>();
		Measurement[] batch;
		int batchIndex;
		for (int i = 0; i < data.size(); i++) {
			// clear batch
			batch = new Measurement[order];
			batchIndex = 0;
			// add actual element to batch
			batch[batchIndex++] = data.get(i);
			// this loop calculates the time difference to each subsequent element
			// if the difference is in the given bounds the element is added to batch and search is ended
			for (int j = i + 1; j < data.size() && batchIndex < order; j++) {
				// calculate time difference
				double timeDiff = data.get(i).timeDiffInSecondsWithSign(data.get(j));
				// if time difference in bounds: add measurement to batch
				if (Math.abs(timeDiff) > MIN_TIME_DIFF_SEC && Math.abs(timeDiff) < MAX_TIME_DIFF_SEC) {
					batch[batchIndex++] = data.get(j);
					/*
					 * if(order == 2 && batchIndex==1 && timeDiff<0){ //swap
					 * Measurement swap = batch[1]; batch[1] = batch[0];
					 * batch[0] = swap; }
					 */
	/*			} else if (Math.abs(timeDiff) > MAX_TIME_DIFF_SEC)
					// when time difference gets to big --> break!
					break;
			}
			// check if there are empty elements in batch
			if (!Arrays.asList(batch).contains(null))
				transformedData.add(measurementsToSimpleMatrix(batch));
		}
		return transformedData;
	}*/
	
	public static LinkedList<SimpleMatrix> embed(List<Measurement> data, int order, int stepSize, int tolerance, int dataPointsNeeded, double minTravelDistance) {
		LinkedList<SimpleMatrix> transformedData = new LinkedList<SimpleMatrix>();
		Measurement[] batch;
		int batchIndex;
		for (int i = 0; i < data.size(); i++) {
			// clear batch
			batch = new Measurement[order];
			batchIndex = 0;
			// add actual element to batch
			batch[batchIndex++] = data.get(i);
			// this loop calculates the time difference to each subsequent element
			// if the difference is in the given bounds the element is added to batch and search is ended
			int k = i;
			for (int j = i + 1; j < data.size() && batchIndex < order; j++) {
				// calculate time difference
				double timeDiff = data.get(k).timeDiffInSecondsWithSign(data.get(j));
				// if time difference in bounds: add measurement to batch
				if (Math.abs(timeDiff) > (stepSize-tolerance) && Math.abs(timeDiff) < (stepSize+tolerance)) {
					batch[batchIndex++] = data.get(j);
					k=j;
					/*
					 * if(order == 2 && batchIndex==1 && timeDiff<0){ //swap
					 * Measurement swap = batch[1]; batch[1] = batch[0];
					 * batch[0] = swap; }
					 */
				} else if (Math.abs(timeDiff) > (stepSize+tolerance))
					// when time difference gets to big --> break!
					break;
			}
			// check if there are empty elements in batch
			if (!Arrays.asList(batch).contains(null)) {
				double travelDistanceSum = differenceVector(batch);
				if(travelDistanceSum > minTravelDistance)
					transformedData.add(measurementsToSimpleMatrix(batch));
				if(transformedData.size() >= dataPointsNeeded)
					return transformedData;
			}
		}
		return transformedData;
	}
	
	
	private static double differenceVector(Measurement[] m){
		SimpleMatrix differenceVector = new SimpleMatrix(m.length-1, 1);
		for(int i=0; i<m.length-1; i++) {
			differenceVector.set(i,0,m[i].distanceInMeters(m[i+1]));
		}
		double travelDistanceSum = 0;
		travelDistanceSum = differenceVector.elementSum();
		return travelDistanceSum;
	}
	
	
	public static LinkedList<SimpleMatrix> transformTSDataFirstOrder(List<Measurement> data, int stepSize, int tolerance, int dataPointsNeeded, double minTravelDistance) {
		int order = 2;
		LinkedList<SimpleMatrix> transformedData = new LinkedList<SimpleMatrix>();
		Measurement[] batch;
		int batchIndex;
		for (int i = 0; i < data.size(); i++) {
			// clear batch
			batch = new Measurement[order];
			batchIndex = 0;
			// add actual element to batch
			batch[batchIndex++] = data.get(i);
			// this loop calculates the time difference to each subsequent element
			// if the difference is in the given bounds the element is added to batch and search is ended
			int k = i;
			for (int j = i + 1; j < data.size() && batchIndex < order; j++) {
				// calculate time difference
				double timeDiff = data.get(k).timeDiffInSecondsWithSign(data.get(j));
				// if time difference in bounds: add measurement to batch
				if (Math.abs(timeDiff) > (stepSize-tolerance) && Math.abs(timeDiff) < (stepSize+tolerance)) {
					batch[batchIndex++] = data.get(j);
					k=j;
					/*
					 * if(order == 2 && batchIndex==1 && timeDiff<0){ //swap
					 * Measurement swap = batch[1]; batch[1] = batch[0];
					 * batch[0] = swap; }
					 */
				} else if (Math.abs(timeDiff) > (stepSize+tolerance))
					// when time difference gets to big --> break!
					break;
			}
			// check if there are empty elements in batch
			if (!Arrays.asList(batch).contains(null)) {
				double travelDistanceSum = differenceVector(batch);
				if(travelDistanceSum > minTravelDistance)
					transformedData.add(measurementsToSimpleMatrixExt(batch));
				if(transformedData.size() >= dataPointsNeeded)
					return transformedData;
			}
		}
		return transformedData;
	}
	
	public static LinkedList<Measurement[]> transformTSDataMeasurements(List<Measurement> data, int order, int stepSize, int tolerance, int dataPointsNeeded, double minTravelDistance) {
		LinkedList<Measurement[]> transformedData = new LinkedList<Measurement[]>();
		Measurement[] batch;
		int batchIndex;
		for (int i = 0; i < data.size(); i++) {
			// clear batch
			batch = new Measurement[order];
			batchIndex = 0;
			// add actual element to batch
			batch[batchIndex++] = data.get(i);
			// this loop calculates the time difference to each subsequent element
			// if the difference is in the given bounds the element is added to batch and search is ended
			int k = i;
			for (int j = i + 1; j < data.size() && batchIndex < order; j++) {
				// calculate time difference
				double timeDiff = data.get(k).timeDiffInSecondsWithSign(data.get(j));
				// if time difference in bounds: add measurement to batch
				if (Math.abs(timeDiff) > (stepSize-tolerance) && Math.abs(timeDiff) < (stepSize+tolerance)) {
					batch[batchIndex++] = data.get(j);
					k=j;
				} else if (Math.abs(timeDiff) > (stepSize+tolerance))
					// when time difference gets to big --> break!
					break;
			}
			// check if there are empty elements in batch
			if (!Arrays.asList(batch).contains(null)) {
				double travelDistanceSum = differenceVector(batch);
				if(travelDistanceSum > minTravelDistance)
					transformedData.add(batch);
				if(transformedData.size() >= dataPointsNeeded)
					return transformedData;
			}
		}
		return transformedData;
	}


	private static SimpleMatrix measurementsToSimpleMatrix(Measurement[] measurements) {
		SimpleMatrix matrix = new SimpleMatrix(measurements.length * 2, 1);
		double lat, lng;
		int row = 0;
		for (int i = 0; i < measurements.length; i++) {
			Measurement m = measurements[i];
			// if(i==0){
			lat = m.getLat();
			lng = m.getLng();
			/*
			 * } else{ lng = m.getLat(); lat = m.getLng(); }
			 */
			matrix.set(row++, 0, lat);
			matrix.set(row++, 0, lng);
		}
		return matrix;
	}
	
	private static SimpleMatrix measurementsToSimpleMatrixExt(Measurement[] measurements) {
		SimpleMatrix matrix = new SimpleMatrix(measurements.length * 2 + 3, 1);
		double lat, lng;
		int fare;
		double speed;
		int timeOfDay;
		double direction;
		int row = 0;
		for (int i = 0; i < measurements.length; i++) {
			Measurement m = measurements[i];
			lat = m.getLat();
			lng = m.getLng();
			
			matrix.set(row++, 0, lat);
			matrix.set(row++, 0, lng);
			if(i == 0) {
				fare = measurements[0].getFare();
				speed = measurements[0].getSpeed();
				timeOfDay = measurements[0].getTimeOfDay();
				direction = measurements[0].getmDirection();//calculateAngle(m,measurements[1]);

				matrix.set(row++, 0, speed);
				matrix.set(row++, 0, timeOfDay);
				matrix.set(row++, 0, direction);
			}
		}
		
		
		return matrix;
	}
	
	private static SimpleMatrix measurementsToSimpleMatrix1(Measurement[] measurements) {
		SimpleMatrix matrix = new SimpleMatrix(measurements.length * 2+1, 1);
		double lat, lng;
		int row = 0;
		for (int i = 0; i < measurements.length; i++) {
			Measurement m = measurements[i];
			// if(i==0){
			lat = m.getLat();
			lng = m.getLng();
			/*
			 * } else{ lng = m.getLat(); lat = m.getLng(); }
			 */
			matrix.set(row++, 0, lat);
			matrix.set(row++, 0, lng);
		}
		matrix.set(row,Math.abs(measurements[1].getDate()-measurements[0].getDate()));
		return matrix;
	}
	
	public static double calculateAngle(Measurement m, Measurement m1) {
		// before distance calcultion projection is necessary
		SimpleMatrix pos = new SimpleMatrix(2, 1);
		SimpleMatrix pos1 = new SimpleMatrix(2, 1);
		pos.set(0, 0, m.getLat());
		pos.set(1, 0, m.getLng());
		pos1.set(0, 0, m1.getLat());
		pos1.set(1, 0, m1.getLng());
		pos = Preprocessor.projectData(pos);
		pos1 = Preprocessor.projectData(pos1);
		double dy = (pos1.get(0, 0) - pos.get(0, 0));
		double dx = (pos1.get(1, 0) - pos.get(1, 0));
		if(dx == 0)
			dx = 1E-10;
		double direction = Math.atan(dy / dx);
		return direction;
	}

}