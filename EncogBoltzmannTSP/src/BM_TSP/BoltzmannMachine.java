package BM_TSP;

public class BoltzmannMachine {
	private int nCities;
	private int mUnits;
	private boolean mOutput[];
	private int mOn[];
	private int mOff[];
	private double mThreshold[];
	private double mWeight[][];
	private double mTemperature;
	private double mGamma;
	private double mDistance[][];
	
	public BoltzmannMachine(int NumCities, double InitialTemperature, double GammaValue)
	{
		 nCities = NumCities;
		 mUnits = nCities * nCities;
		 mTemperature = InitialTemperature;
		 mGamma = GammaValue;
		
		 mOutput = new boolean[mUnits];
		 mOn = new int[mUnits];
		 mOff = new int[mUnits];
		 mThreshold = new double[mUnits];
		 mWeight = new double[mUnits][mUnits];
		 mDistance = new double[nCities][nCities];

		 return;
	}
	
	private boolean RandomEqualBool()
	{
    	if(Math.random() >= 0.5){
        	return true;
    	}else{
        	return false;
    	}
	}
	
	private int RandomEqualInt(int Low, int High)
	{
		return (int)(High * Math.random()) + Low;
	}

	private double RandomEqualDouble(double Low, double High)
	{
		return (High * Math.random()) + Low;
	}
	
	public void CalcWeights()
	{
		int i, j, n1, n2, n3, n4;
		int Pred_n3, Succ_n3;
		double Weight;

	    for(n1 = 0; n1 < nCities; n1++)
	    {
	        for(n2 = 0; n2 < nCities; n2++)
	        {
	            i = n1 * nCities + n2;
	            for(n3 = 0; n3 < nCities; n3++)
	            {
	                for(n4 = 0; n4 < nCities; n4++)
	                {
	                    j = n3 * nCities + n4;
	                    Weight = 0.0;
	                    if(i != j){
	                        if(n3 == 0){
	                            Pred_n3 = nCities - 1;
	                        }else{
	                            Pred_n3 = n3 - 1;
	                        }
	                        if(n3 == nCities - 1){
	                            Succ_n3 = 0;
	                        }else{
	                            Succ_n3 = n3 + 1;
	                        }
	                        if((n1 == n3) || (n2 == n4)){
	                            Weight = -mGamma;
	                        }else if((n1 == Pred_n3) || (n1 == Succ_n3)){
	                            Weight = -mDistance[n2][n4];
	                        }
	                    }
	                    mWeight[i][j] = Weight;
	                } // n4
	            } // n3
	            mThreshold[i] = (-mGamma / 2);
	        } // n2
	    } // n1

	    return;
	}
	
	public double LengthOfTour()
	{
		double Length = 0.0;
		int n1, n2, n3;
		
	    for(n1 = 0; n1 < nCities; n1++)
	    {
	        for(n2 = 0; n2 < nCities; n2++)
	        {
	            if(mOutput[((n1) % nCities) * nCities + n2]){
	                //Exit For
	            	break;
	            }
	        } // n2

	        for(n3 = 0; n3 < nCities; n3++)
	        {
	            if(mOutput[((n1 + 1) % nCities) * nCities + n3]){
	            	break;
	            }
	        } // n3
	        
	        Length += mDistance[n2][n3];
	        
	    } // n1

	    return Length;
	}
	
	public void SetRandom()
	{
		int i;
	    for(i = 0; i < mUnits; i++)
	    {
	        mOutput[i] = RandomEqualBool();
	    }
	    return;
	}
	
	public void PropagateUnit(int i)
	{
		double Sum = 0.0;
		double Probability;
		int j;
		
	    for(j = 0; j < mUnits; j++){
	        if(mOutput[j] == true){
	            Sum += mWeight[i][j] * 1.0;
	        }else{
	            Sum += mWeight[i][j] * 0.0;
	        }
		} // j
	    Sum -= mThreshold[i];

	    Probability = 1 / (1 + Math.exp(-Sum / mTemperature));

	    if(RandomEqualDouble(0.0, 1.0) <= Probability){
	        mOutput[i] = true;
	    }else{
	        mOutput[i] = false;
	    }
	    return;
	}
	
	public void ReduceHeat()
	{
		int index;
		int i, n;
	    for(i = 0; i < mUnits; i++)
	    {
	        mOn[i] = 0;
	        mOff[i] = 0;
	    } // i

	    for(n = 0; n < 1000 * mUnits; n++)
	    {
	        index = RandomEqualInt(0, mUnits - 1);
	        PropagateUnit(index);
	    } // n

	    for(n = 0; n < 100 * mUnits; n++)
	    {
	        index = RandomEqualInt(0, mUnits - 1);
	        PropagateUnit(index);
	        if(mOutput[index]){
	            mOn[index] += 1;
	        }else{
	            mOff[index] += 1;
	        }
	    } // n

	    for(i = 0; i < mUnits; i++)
	    {
	        mOutput[i] = (mOn[i] > mOff[i]);
	    } // i
	    return;
	}
	
	public int Cities()
	{
    	return nCities;
	}
	
	public int Units()
	{
	    return mUnits;
	}
	
	public void Units(int Value)
	{
		mUnits = Value;
		return;
	}
	
	public boolean Output(int index)
	{
        return mOutput[index];
	}
	
    public void Output(int index, boolean Value)
    {
        mOutput[index] = Value;
        return;
    }
    
    public int NodeOn(int index)
    {
        return mOn[index];
    }
    
    public void NodeOn(int index, int Value)
    {
        mOn[index] = Value;
        return;
    }
    
    public int NodeOff(int index)
    {
        return mOff[index];
    }
    
    public void NodeOff(int index, int Value)
    {
        mOff[index] = Value;
        return;
    }

    public double Threshold(int index)
    {
        return mThreshold[index];
    }
    
    public void Threshold(int index, double Value)
    {
        mThreshold[index] = Value;
        return;
    }   
	
    public double Weight(int x, int y)
    {
        return mWeight[x][y];
    }
    
    public void Weight(int x, int y, double Value)
    {
        mWeight[x][y] = Value;
        return;
    }

    public double Temperature()
    {
        return mTemperature;
    }
    
    public void Temperature(double Value)
    {
        mTemperature = Value;
        return;
    }

    public double Distance(int x, int y)
    {
        return mDistance[x][y];
    }
    
    public void Distance(int x, int y, double Value)
    {
        mDistance[x][y] = Value;
        return;
    }
	
}
