package com.juans.inspeccion.data.remote.sqlserver;

import com.juans.inspeccion.Mundo.DaniosManager;

import android.content.res.Resources;
import com.juans.inspeccion.data.remote.sqlserver.ConnectionException;
import com.juans.inspeccion.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.HashMap;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=android.app.Application.class)
public class DamageCostLookupTest {
    private Resources res;private HashMap<String,String> damage,header,container;
    @Before public void setup(){res=RuntimeEnvironment.getApplication().getResources();damage=new HashMap<>();header=new HashMap<>();container=new HashMap<>();damage.put(res.getString(R.string.CCODMET),"TEST");damage.put(res.getString(R.string.NUNIDADES),"2");header.put(res.getString(R.string.CCTELNA),"TEST");container.put(res.getString(R.string.CTAMCNTR),"20");}
    private class Rates implements DamageCostLookup.Source {
        boolean missing,broken; int reads;
        @Override public String single(String query) throws ConnectionException {reads++;if(broken)throw new ConnectionException("test outage");if(query.startsWith(DAO.TARIFA_DE_LINEA))return "5";return "1";}
        @Override public HashMap<String,String> costs(Resources r,HashMap<String,String>d,HashMap<String,String>h){reads++;assertEquals("20",h.get(r.getString(R.string.CTAMCNTR)));if(missing)return new HashMap<>();HashMap<String,String>rate=new HashMap<>();rate.put(r.getString(R.string.NCOSTO),"10");rate.put(r.getString(R.string.NHORASHOM),"1");rate.put(r.getString(R.string.NTARIVA),"10");rate.put(r.getString(R.string.CMONEDA),"TEST");rate.put(r.getString(R.string.CCPTOLINEA),"TEST");return rate;}
    }
    // DAMAGE-004: real calculation consumes deterministic external rates, not a fake calculator.
    @Test public void quantityMultipliesMaterialAndLaborBeforeTax(){Rates source=new Rates();assertTrue(DamageCostLookup.calculate(res,damage,header,container,false,source));assertEquals("20.0",damage.get(res.getString(R.string.NVALOR)));assertEquals("10.0",damage.get(res.getString(R.string.NCOSHORHOM)));assertEquals("33.0",damage.get(DaniosManager.COSTO_TOTAL_DANIO));}
    @Test public void noAplicaDoesNotReadRatesAndResetsPreviousAmounts(){Rates source=new Rates();damage.put(DaniosManager.COSTO_TOTAL_DANIO,"99");assertFalse(DamageCostLookup.calculate(res,damage,header,container,true,source));assertEquals(0,source.reads);assertEquals("0",damage.get(DaniosManager.COSTO_TOTAL_DANIO));assertEquals("0",damage.get(res.getString(R.string.NVALOR)));}
    @Test public void characterizationMissingTariffLeavesZeroAmounts(){Rates source=new Rates();source.missing=true;assertFalse(DamageCostLookup.calculate(res,damage,header,container,false,source));assertEquals("0",damage.get(DaniosManager.COSTO_TOTAL_DANIO));}
    @Test public void characterizationRateConnectionFailureLeavesZeroAmounts(){Rates source=new Rates();source.broken=true;assertFalse(DamageCostLookup.calculate(res,damage,header,container,false,source));assertEquals("0",damage.get(DaniosManager.COSTO_TOTAL_DANIO));assertEquals("0",damage.get(res.getString(R.string.NTARIVA)));}
    @Test public void blankQuantityCurrentlyProducesZeroRatherThanValidation(){Rates source=new Rates();damage.put(res.getString(R.string.NUNIDADES),"");assertTrue(DamageCostLookup.calculate(res,damage,header,container,false,source));assertEquals("0.0",damage.get(DaniosManager.COSTO_TOTAL_DANIO));}
}
