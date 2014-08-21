
package vopen.db;

public class WhereStringBuilder {
    private StringBuilder mSelectWhere = new StringBuilder(" ");

    @Override
    public String toString() {
        return mSelectWhere.toString();
    }

    /**
     * 增加一个 ")"
     * 
     * @return
     */
    public WhereStringBuilder rP() {
        mSelectWhere.append(")");
        return this;
    }
    
    /**
     * 增加一个 "("
     * 
     * @return
     */
    public WhereStringBuilder lP() {
        mSelectWhere.append("(");
        return this;
    }
    
    /**
     * 增加一个 " OR "
     * 
     * @return
     */
    public WhereStringBuilder or() {
        mSelectWhere.append(" OR ");
        return this;
    }

    /**
     * 增加一个 " AND "
     * 
     * @return
     */
    public WhereStringBuilder and() {
        mSelectWhere.append(" AND ");
        return this;
    }

    /**
     * 增加long变量
     * 
     * @param column
     * @param value
     * @return
     */
    public WhereStringBuilder append(String column, long value) {
        mSelectWhere.append(column).append("=").append(value);
        return this;
    }

    /**
     * 增加int变量
     * 
     * @param column
     * @param value
     * @return
     */
    public WhereStringBuilder append(String column, int value) {
        mSelectWhere.append(column).append("=").append(value);
        return this;
    }

    /**
     * 增加String变量<br/>
     * " xxx=?"形式<br/>
     * 
     * @param column
     * @param value
     * @return
     */
    public WhereStringBuilder append(String column) {
        mSelectWhere.append(column).append("=").append("?");
        return this;
    }
}
