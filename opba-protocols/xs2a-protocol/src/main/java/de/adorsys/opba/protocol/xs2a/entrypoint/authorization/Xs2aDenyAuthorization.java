package de.adorsys.opba.protocol.xs2a.entrypoint.authorization;

import de.adorsys.opba.protocol.api.authorization.DenyAuthorization;
import de.adorsys.opba.protocol.api.dto.context.ServiceContext;
import de.adorsys.opba.protocol.api.dto.request.authorization.DenyAuthorizationRequest;
import de.adorsys.opba.protocol.api.dto.result.body.DenyAuthBody;
import de.adorsys.opba.protocol.api.dto.result.fromprotocol.Result;
import de.adorsys.opba.protocol.api.dto.result.fromprotocol.dialog.AuthorizationDeniedResult;
import de.adorsys.opba.protocol.xs2a.service.xs2a.consent.AbortConsent;
import de.adorsys.opba.protocol.xs2a.service.xs2a.context.ais.Xs2aAisContext;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static de.adorsys.opba.protocol.xs2a.constant.GlobalConst.CONTEXT;

@Service("xs2aDenyAuthorization")
@RequiredArgsConstructor
public class Xs2aDenyAuthorization implements DenyAuthorization {

    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final AbortConsent abortConsent;

    @Override
    public CompletableFuture<Result<DenyAuthBody>> execute(ServiceContext<DenyAuthorizationRequest> serviceContext) {
        String executionId = serviceContext.getAuthContext();

        Xs2aAisContext ctx = readContext(executionId);
        abortConsent.abortConsent(ctx);

        return CompletableFuture.completedFuture(
                new AuthorizationDeniedResult<>(
                        URI.create(ctx.getFintechRedirectUriNok()),
                        new DenyAuthBody()
                )
        );
    }

    private Xs2aAisContext readContext(String executionId) {
        if (null != runtimeService.createExecutionQuery().executionId(executionId).singleResult()) {
            return (Xs2aAisContext) runtimeService.getVariable(executionId, CONTEXT);
        }

        return readHistoricalContext(executionId);
    }

    private Xs2aAisContext readHistoricalContext(String executionId) {
        HistoricActivityInstance finished = historyService.createHistoricActivityInstanceQuery()
                .executionId(executionId)
                .finished()
                .listPage(0, 1)
                .get(0);

        return (Xs2aAisContext) historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(finished.getProcessInstanceId())
                .variableName(CONTEXT)
                .singleResult()
                .getValue();
    }
}