package elevator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/elevator")
public class ElevatorApiController {

    @PostMapping("/{id}/dispatch")
    public void dispatch(
            @PathVariable char id,
            @RequestBody DispatchInstructionDto instruction) {

        // Η εντολή θα προωθηθεί στον συγκεκριμένο elevator.
    }

    @RequestMapping(value = "/{id}/status", method = RequestMethod.GET)
    public ElevatorStatusDto getStatus(
            @PathVariable char id) {

        // Επιστρέφει την τρέχουσα κατάσταση του elevator.
        return null;
    }

    @PutMapping("/{id}/config")
    public void updateConfig(
            @PathVariable char id,
            @RequestBody ElevatorConfigDto config) {

        // Ενημερώνει τις παραμέτρους λειτουργίας.
    }
}