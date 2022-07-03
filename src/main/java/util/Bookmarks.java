package util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bookmarks implements Comparable{
    private String nameOfPage;
    private Integer pageNo;

    @Override
    public int compareTo(Object bookmarks) {
        return this.pageNo - ((Bookmarks)bookmarks).pageNo;
    }
}
