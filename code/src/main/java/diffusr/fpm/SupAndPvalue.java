package diffusr.fpm;


/** A class to store a support and p-value pair. */
public class SupAndPvalue {

    final int sup;
    final double pvalue;

    public SupAndPvalue(int sup, double pvalue) {
        this.sup = sup;
        this.pvalue = pvalue;
    }
}
