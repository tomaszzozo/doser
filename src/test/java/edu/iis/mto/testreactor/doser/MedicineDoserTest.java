package edu.iis.mto.testreactor.doser;

import edu.iis.mto.testreactor.doser.infuser.Infuser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MedicineDoserTest {
    @Mock private Infuser infuser;
    @Mock private DosageLog dosageLog;
    @Mock private Clock clock;

    private final Medicine standardMedicine = Medicine.of("Cough syrup");
    private final Capacity standardCapacity = Capacity.of(500, CapacityUnit.MILILITER);
    private final MedicinePackage standardMedicinePackage = MedicinePackage.of(standardMedicine, standardCapacity);

    private MedicineDoser medicineDoser;

    @BeforeEach
    void setUp() {
       medicineDoser = new MedicineDoser(infuser, dosageLog, clock);
    }

    @Test
    void medicineDoserNullParameters() {
        assertThrows(NullPointerException.class, () -> new MedicineDoser(null, dosageLog, clock));
        assertThrows(NullPointerException.class, () -> new MedicineDoser(infuser, null, clock));
        assertThrows(NullPointerException.class, () -> new MedicineDoser(infuser, dosageLog, null));
        assertDoesNotThrow(() -> new MedicineDoser(infuser, dosageLog, clock));
    }
}
