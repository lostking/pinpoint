package com.nhn.pinpoint.profiler.modifier.db.oracle;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.JDBCScopeDelegateSimpleInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

public class OracleStatementWrapperModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OracleStatementWrapperModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "oracle/jdbc/driver/OracleStatementWrapper";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass statementClass = byteCodeInstrumentor.getClass(javassistClassName);
            Interceptor executeQuery = new JDBCScopeDelegateSimpleInterceptor(new StatementExecuteQueryInterceptor());
            statementClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, executeQuery);

            // TODO 이거 고쳐야 됨.
            Interceptor executeUpdate1 = new JDBCScopeDelegateSimpleInterceptor((SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor"));
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String"}, executeUpdate1);

            Interceptor executeUpdate2 = new JDBCScopeDelegateSimpleInterceptor((SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor"));
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String", "int"}, executeUpdate2);

            Interceptor execute1 = new JDBCScopeDelegateSimpleInterceptor((SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor"));
            statementClass.addInterceptor("execute", new String[]{"java.lang.String"}, execute1);

            Interceptor execute2 = new JDBCScopeDelegateSimpleInterceptor((SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor"));
            statementClass.addInterceptor("execute", new String[]{"java.lang.String", "int"}, execute2);

            statementClass.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.Object");
            return statementClass.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }


}
