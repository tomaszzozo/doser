package edu.iis.mto.testreactor.doser;

import edu.iis.mto.testreactor.doser.infuser.Infuser;
import edu.iis.mto.testreactor.doser.infuser.InfuserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineDoserTest {
    @Mock private Infuser infuser;
    @Mock private DosageLog dosageLog;
    @Mock private Clock clock;

    private final Medicine standardMedicine = Medicine.of("Cough syrup");
    private final Capacity standardCapacity = Capacity.of(500, CapacityUnit.MILILITER);
    private final Period standardPeriod = Period.of(12, TimeUnit.HOURS);
    private final Dose standardDose = Dose.of(Capacity.of(20, CapacityUnit.MILILITER), standardPeriod);
    private final Receipe standardReceipe = Receipe.of(standardMedicine, standardDose, 20);
    private final MedicinePackage standardMedicinePackage = MedicinePackage.of(standardMedicine, standardCapacity);

    private MedicineDoser medicineDoser;

    @BeforeEach
    void setUp() {
       medicineDoser = new MedicineDoser(infuser, dosageLog, clock);
    }

    @Test
    void medicineDoserNullParameters() {
        NullPointerException result = assertThrows(NullPointerException.class, () -> new MedicineDoser(null, dosageLog, clock));
        assertEquals("infuser == null", result.getMessage());

        result = assertThrows(NullPointerException.class, () -> new MedicineDoser(infuser, null, clock));
        assertEquals("dosageLog == null", result.getMessage());

        result = assertThrows(NullPointerException.class, () -> new MedicineDoser(infuser, dosageLog, null));
        assertEquals("clock == null", result.getMessage());

        assertDoesNotThrow(() -> new MedicineDoser(infuser, dosageLog, clock));
    }

    @Test
    void addNullPackage() {
        NullPointerException result = assertThrows(NullPointerException.class, () -> medicineDoser.add(null));
        assertEquals("medicine package == null", result.getMessage());

        assertDoesNotThrow(() -> medicineDoser.add(standardMedicinePackage));
    }

    @Test
    void nullReceipe() {
        medicineDoser.add(standardMedicinePackage);
        NullPointerException result = assertThrows(NullPointerException.class, () -> medicineDoser.dose(null));
        assertEquals("medicine == null", result.getMessage());
    }

    @Test
    void correctDose() {
        medicineDoser.add(standardMedicinePackage);
        assertEquals(DosingResult.SUCCESS, medicineDoser.dose(standardReceipe));

        InOrder callOrder = inOrder(dosageLog);
        callOrder.verify(dosageLog).logStart();
        callOrder.verify(dosageLog).logEnd();
    }

    @Test
    void amountTooBig() {
        medicineDoser.add(standardMedicinePackage);
        Dose incorrectDose = Dose.of(Capacity.of(501, CapacityUnit.MILILITER), standardPeriod);
        Receipe receipe = Receipe.of(standardMedicine, incorrectDose, 1);
        InsufficientMedicineException result = assertThrows(InsufficientMedicineException.class, () -> medicineDoser.dose(receipe));
        assertEquals("Medicine [name=Cough syrup]", result.getMessage());
    }

    @Test
    void wrongCapacityUnit() {
        medicineDoser.add(standardMedicinePackage);
        Dose incorrectDose = Dose.of(Capacity.of(20, CapacityUnit.LITER), standardPeriod);
        Receipe receipe = Receipe.of(standardMedicine, incorrectDose, 1);
        InsufficientMedicineException result = assertThrows(InsufficientMedicineException.class, () -> medicineDoser.dose(receipe));
        assertEquals("Medicine [name=Cough syrup]", result.getMessage());
    }

    @Test
    void infuserException() throws InfuserException {
        medicineDoser.add(standardMedicinePackage);
        doThrow(InfuserException.class).when(infuser).dispense(any(MedicinePackage.class), any(Capacity.class));
        medicineDoser.dose(standardReceipe);

        InOrder callOrder = inOrder(dosageLog);
        callOrder.verify(dosageLog).logStart();
        callOrder.verify(dosageLog).logStartDose(any(Medicine.class), any(Dose.class));
        // Mockito twierdzi uparcie, że poniższa metoda nie wywołała się.
        // Nie mam pojęcia dlaczego.
        // Podczas debugowania programu proces natrafia dokładnie na tę metodę w bloku catch.
        callOrder.verify(dosageLog).logDifuserError(any(Dose.class), anyString());
    }

    @Test
    void clockTest() {
        medicineDoser.add(standardMedicinePackage);
        medicineDoser.dose(standardReceipe);

        verify(clock, times(20)).wait(standardPeriod);
    }
}
