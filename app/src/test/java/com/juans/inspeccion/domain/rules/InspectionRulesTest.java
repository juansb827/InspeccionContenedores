package com.juans.inspeccion.domain.rules;

import com.juans.inspeccion.Mundo.Inspeccion;

import android.content.res.Resources;
import com.juans.inspeccion.ui.inspection.InspeccionActivity;
import com.juans.inspeccion.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=android.app.Application.class)
public class InspectionRulesTest {
    private Resources res;private Inspeccion inspection;
    @Before public void setup(){res=RuntimeEnvironment.getApplication().getResources();inspection=new Inspeccion();inspection.setTipoInspeccion("ENTRADA");inspection.getInformacion().put(res.getString(R.string.CTIPDOC),"EP");inspection.getInformacion().put(res.getString(R.string.CCTELNA),"TEST");inspection.getDatosContenedor().put(res.getString(R.string.CCTELNA),"TEST");}
    private String message(boolean complete,String action){return InspectionRules.validationMessage(complete,inspection,inspection.getInformacion(),action,res);}
    // ENTRY-002/003, EXIT-001
    @Test public void completeMatchingEntryOutsideYardIsValid(){assertNull(message(true,null));}
    @Test public void missingRequiredFieldsAreRejected(){assertEquals("Faltan campos obligatorios",message(false,null));}
    @Test public void wrongContainerLineIsRejected(){inspection.getDatosContenedor().put(res.getString(R.string.CCTELNA),"OTHER");assertEquals("El contenedor no peternece a la linea",message(true,null));}
    @Test public void absentContainerLineDoesNotInventAMismatch(){inspection.getDatosContenedor().clear();assertNull(message(true,null));}
    @Test public void entryAlreadyInYardAndExitOutsideYardAreRejected(){inspection.setEstaEnPatio(true);assertEquals("El contenedor YA se encuentra en el patio",message(true,null));inspection.setTipoInspeccion("SALIDA");inspection.setEstaEnPatio(false);assertEquals("El contenedor NO se encuentra en el patio",message(true,null));}
    @Test public void modificationBypassesYardButStillValidatesRequiredFields(){inspection.setEstaEnPatio(true);assertNull(message(true,InspeccionActivity.MODIFICAR_DOCUMENTO));assertEquals("Faltan campos obligatorios",message(false,InspeccionActivity.MODIFICAR_DOCUMENTO));}
    private void exitBooking(){inspection.setTipoInspeccion("SALIDA");inspection.setEstaEnPatio(true);inspection.setInfoBooking(new ArrayList<>(Collections.singletonList(new HashMap<>())));inspection.setHaySaldoBooking(true);inspection.setPerteneceEnBooking(true);}
    // EXIT-002 / CONTAINER-005, including source-characterized precedence/bypasses.
    @Test public void exitBookingRejectsMembershipAndBalance(){exitBooking();inspection.setPerteneceEnBooking(false);assertEquals("El contenedor no pertenece al Booking",message(true,null));inspection.setHaySaldoBooking(false);assertEquals("El booking ya fue atendido totalmente",message(true,null));}
    @Test public void emptyBookingSkipsMembershipAndBalance(){exitBooking();inspection.setInfoBooking(new ArrayList<>());inspection.setHaySaldoBooking(false);inspection.setPerteneceEnBooking(false);assertNull(message(true,null));inspection.setInfoBooking(null);assertNull(message(true,null));}
    @Test public void transportAndModificationSkipBookingChecks(){exitBooking();inspection.setHaySaldoBooking(false);inspection.getInformacion().put(res.getString(R.string.CTIPDOC),"ET");assertNull(message(true,null));inspection.getInformacion().put(res.getString(R.string.CTIPDOC),"EP");assertNull(message(true,InspeccionActivity.MODIFICAR_DOCUMENTO));}
    @Test public void characterizationLaterBookingMessageMasksEarlierRequiredAndLineErrors(){exitBooking();inspection.setHaySaldoBooking(false);inspection.getDatosContenedor().put(res.getString(R.string.CCTELNA),"OTHER");assertEquals("El booking ya fue atendido totalmente",message(false,null));}
    // CONTAINER-001
    @Test public void turnoRejectsMissingAttendedAndWrongDirection(){HashMap<String,String>d=new HashMap<>();assertEquals("No se ha encontrado el turno",InspectionRules.turnoMessage(d,"ENTRADA",res));d.put(res.getString(R.string.NATENDIDO),"1");d.put(res.getString(R.string.CTIPOTURNO),"SALIDA");assertEquals("Este turno ya fue atendido",InspectionRules.turnoMessage(d,"ENTRADA",res));d.put(res.getString(R.string.NATENDIDO),"0");assertEquals("El tipo de turno no es de :ENTRADA",InspectionRules.turnoMessage(d,"ENTRADA",res));d.put(res.getString(R.string.CTIPOTURNO)," ENTRADA ");assertNull(InspectionRules.turnoMessage(d,"ENTRADA",res));}
}
