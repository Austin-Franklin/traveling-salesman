package cooling_functions;

public interface CoolingFunction extends Cloneable {
    public double cool();
    public double getTemp();
    public CoolingFunction clone();
}