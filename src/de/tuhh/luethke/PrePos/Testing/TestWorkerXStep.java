package de.tuhh.luethke.PrePos.Testing;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Callable;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import de.tuhh.luethke.PrePos.utility.Measurement;
import de.tuhh.luethke.Prediction.Prediction;
import de.tuhh.luethke.Prediction.Predictor;
import de.tuhh.luethke.oKDE.model.SampleModel;

public class TestWorkerXStep implements Callable<Double> {

	private Measurement[] mMeasurements;
	SampleModel mModel;

	public TestWorkerXStep(Measurement[] measurements, SampleModel model) {
		try {
			this.mModel = new SampleModel(model);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.mMeasurements = measurements;
	}

	@Override
	public Double call() throws Exception {
		/*long time = System.currentTimeMillis();
		double[][] dxVector = new double[4][1];
		dxVector[0][0] = mMeasurements.get(0).getLat();
		dxVector[1][0] = mMeasurements.get(0).getLng();
		dxVector[2][0] = mMeasurements.get(1).getLat();
		dxVector[3][0] = mMeasurements.get(1).getLng();
		SimpleMatrix pointVector = new SimpleMatrix(dxVector);
		SimpleMatrix pointVector1 = Preprocessor.projectData4(pointVector);
		dxVector = new double[2][1];
		dxVector[0][0] = mMeasurements.get(2).getLat();
		dxVector[1][0] = mMeasurements.get(2).getLng();
		pointVector = new SimpleMatrix(dxVector);
		SimpleMatrix pointVector2 = Preprocessor.projectData(pointVector);
		double prob = mModel.trapezoidRule(pointVector1, pointVector2, 100, 100);
		System.out.println("time: "+(System.currentTimeMillis()-time));
		return prob;*/
		Predictor predictor = new Predictor(mModel);
		Measurement[] samples = new Measurement[mMeasurements.length-1];
		for(int i=0; i<samples.length; i++){
			samples[i] = mMeasurements[i];
		}
		Prediction pre = predictor.predict(samples);
		
		LatLng predictionPoint = new LatLng(pre.latitude, pre.longitude);
		
		LatLng[] samplePoints = new LatLng[mMeasurements.length];
		for(int i=0; i<samplePoints.length; i++){
			samplePoints[i] = new LatLng(mMeasurements[i].getLat(), mMeasurements[i].getLng());
		}
		double[] distances = new double[samplePoints.length];
		for(int i=0; i<distances.length-1; i++){
			distances[i] = LatLngTool.distance(samplePoints[i], samplePoints[i+1], LengthUnit.METER);
		}
		distances[distances.length-1] = LatLngTool.distance(samplePoints[samplePoints.length-1], predictionPoint, LengthUnit.METER);
		double relDist = distances[distances.length-1]/distances[distances.length-2];

		
		String coordianteString = "";
		for(int i=0; i<mMeasurements.length; i++) {
			coordianteString = coordianteString + mMeasurements[i].getLat()+" "+ mMeasurements[i].getLng()+" | ";
		}
		coordianteString = coordianteString + pre.latitude+" "+ pre.longitude;

		String distanceString = "";
		for(int i=0; i<distances.length; i++) {
			distanceString = distanceString + distances[i]+" ";
		}
		
		String probString = relDist+" "+pre.marginalProbability+" "+pre.probability+" "+pre.widerProbability;
		
		System.out.println(coordianteString+"\n"+distanceString+"\n"+probString);
		
		return distances[distances.length-1];
	}

}