package cooling_functions;

public class ExponentialCooling implements CoolingFunction {
    double temp;
    double coolingRate;

    public ExponentialCooling(double initialTemp, double coolingRate) {
        this.temp = initialTemp;
        this.coolingRate = coolingRate > 0 ? coolingRate :  -1 * coolingRate;
    }

    @Override
    public double cool() {
        temp *= Math.exp(-1 * coolingRate);
        return temp;
    }

    @Override
    public double getTemp() {
        return temp;
    }

    
}