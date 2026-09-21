package ke.eworks.ripoti.audit;

import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuditPageDTO {

    private List<AuditDTO> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

}
