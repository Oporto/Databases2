package simpledb.parse;

import simpledb.metadata.IndexType;

/**
 * The parser for the <i>create index</i> statement.
 * @author Edward Sciore
 */
public class CreateIndexData {
   private String idxname, tblname, fldname;
   private IndexType type;
   
   /**
    * Saves the table and field names of the specified index.
    */
   public CreateIndexData(String idxname, String tblname, String fldname, IndexType type) {
      this.idxname = idxname;
      this.tblname = tblname;
      this.fldname = fldname;
      this.type = type;
   }
   
   /**
    * Returns the name of the index.
    * @return the name of the index
    */
   public String indexName() {
      return idxname;
   }
   
   /**
    * Returns the name of the indexed table.
    * @return the name of the indexed table
    */
   public String tableName() {
      return tblname;
   }
   
   /**
    * Returns the name of the indexed field.
    * @return the name of the indexed field
    */
   public String fieldName() {
      return fldname;
   }

   /**
    * Returns the type of the index.
    * @return the type of the index
    */
   public IndexType indexType() {
      return type;
   }
}

