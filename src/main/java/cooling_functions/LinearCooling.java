package cooling_functions;

public class LinearCooling implements CoolingFunction {
    double temp;
    double coolingRate;

    public LinearCooling(double initialTemp, double coolingRate) {
        this.temp = initialTemp;
        this.coolingRate = coolingRate > 0 ? coolingRate : -1 * coolingRate;
    }

    public double cool() {
        temp = temp - coolingRate > 0 ? temp - coolingRate : 0;
        return temp;
    }

    public double getTemp() {
        return temp;
    }

    @Override
    public LinearCooling clone()  {
        return this.clone();
    }
}