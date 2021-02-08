// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.cache;

import java.util.HashSet;
import java.util.Set;

class SimpleStatementParser {
    Set<String> getTablesOfSelectStatement(String sql) {
        // Create the result Set
        // 创建一个sql结果set
        Set<String> result = new HashSet<String>();
        // Normalize the string (remove all double whitespaces)
        // 规范化字符串（删除所有双空格）
        while (true) {
            String newsql = sql.replaceAll("  ", " ");
            if (newsql.equals(sql)) {
                break;
            } else {
                sql = newsql;
            }
        }
        // Now parse the string for the used tables
        // 现在解析所用表的字符串
        String sqlLowerCase = sql.toLowerCase();
        int selectPos = sqlLowerCase.indexOf("select");

        // Any SELECT-Portion ?
        // 任何SELECT部分？
        if (selectPos >= 0) {
            // Now search for JOIN-Parts
            // 现在搜索JOIN-Parts
            boolean fromLoop = true;
            int fromStartPos = 0;

            while (fromLoop) {
                int fromPos = sqlLowerCase.indexOf(" from ", fromStartPos);

                if (fromPos >= 0) {
                    String fromPart = sqlLowerCase.substring(fromPos + 6);
                    String[] parts = fromPart.split(",");

                    for (String s : parts) {
                        String part = s.trim();
                        int spacePos = part.indexOf(' ');
                        if (spacePos >= 0) {
                            String table = part.substring(0, spacePos);
                            result.add(table.toLowerCase());
                        } else {
                            result.add(part.toLowerCase());
                        }
                    }

                    fromStartPos = fromPos + 6;
                } else {
                    fromLoop = false;
                }
            }

            // Now search for JOIN-Parts
            // 现在搜索JOIN-Parts
            boolean joinLoop = true;
            int joinStartPos = 0;

            while (joinLoop) {
                int joinPos = sqlLowerCase.indexOf(" join ", joinStartPos);

                if (joinPos >= 0) {
                    int joinPos2 = sql.indexOf(' ', joinPos + 6);

                    String tableName;
                    if (joinPos2 >= 0) {
                        tableName = sql.substring(joinPos + 6, joinPos2);
                    } else {
                        tableName = sql.substring(joinPos + 6);
                    }

                    result.add(tableName.toLowerCase());

                    joinStartPos = joinPos2;
                } else {
                    joinLoop = false;
                }
            }
        }

        return result;
    }

    private static void dumpTables(String sql) {
        System.out.println(sql);
        SimpleStatementParser parser = new SimpleStatementParser();
        Set<String> tables = parser.getTablesOfSelectStatement(sql);
        for (String table : tables) {
            System.out.println("Table: " + table);
        }
    }

    public static void main(String[] args) {
        dumpTables("SELECT * FROM Test");
        dumpTables("SELECT * FROM Test, Jolly WHERE H = O");
        dumpTables("SELECT * FROM Test INNER JOIN Jolly WHERE H = O");
        dumpTables("     SELECT * FROM Test    INNER    JOIN    Jolly ON Test.Id = Jolly.Id   INNER   JOIN     Opa WHERE H = O");
        dumpTables("select usr.user_id as x0_0_ from Users usr where usr.discriminator='kport' and ((65543 in(select register0_.regcat_id from rel_usr_cat register0_ where usr.user_id=register0_.user_id))");
    }
}