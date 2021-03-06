/*
 * Copyright (c) 2017 Practice Insight Pty Ltd.
 */

package pi.analytics.admin.serviceaddress.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pi.admin.service_address_sorting.generated.AssignServiceAddressRequest;
import pi.admin.service_address_sorting.generated.AssignServiceAddressResponse;
import pi.admin.service_address_sorting.generated.CreateLawFirmRequest;
import pi.admin.service_address_sorting.generated.CreateLawFirmResponse;
import pi.admin.service_address_sorting.generated.NextUnsortedServiceAddressRequest;
import pi.admin.service_address_sorting.generated.SearchLawFirmsRequest;
import pi.admin.service_address_sorting.generated.SearchResults;
import pi.admin.service_address_sorting.generated.ServiceAddressBundle;
import pi.admin.service_address_sorting.generated.ServiceAddressSortingServiceGrpc;
import pi.admin.service_address_sorting.generated.SetInsufficientInfoStatusRequest;
import pi.admin.service_address_sorting.generated.SetInsufficientInfoStatusResponse;
import pi.admin.service_address_sorting.generated.SetServiceAddressAsNonLawFirmRequest;
import pi.admin.service_address_sorting.generated.SetServiceAddressAsNonLawFirmResponse;
import pi.admin.service_address_sorting.generated.SkipServiceAddressRequest;
import pi.admin.service_address_sorting.generated.SkipServiceAddressResponse;
import pi.admin.service_address_sorting.generated.UnsortServiceAddressRequest;
import pi.admin.service_address_sorting.generated.UnsortServiceAddressResponse;
import pi.analytics.admin.serviceaddress.metrics.ImmutableMetricSpec;
import pi.analytics.admin.serviceaddress.metrics.MetricSpec;
import pi.analytics.admin.serviceaddress.metrics.MetricsAccessor;
import pi.analytics.admin.serviceaddress.service.law_firm.LawFirmRepository;
import pi.analytics.admin.serviceaddress.service.service_address.ServiceAddressBundleFetcher;
import pi.analytics.admin.serviceaddress.service.service_address.ServiceAddressSorter;
import pi.analytics.admin.serviceaddress.service.service_address.SortableServiceAddressFetcher;

/**
 * @author shane.xie@practiceinsight.io
 */
@Singleton
public class ServiceAddressSortingServiceImpl extends ServiceAddressSortingServiceGrpc.ServiceAddressSortingServiceImplBase {

  private static final Logger log = LoggerFactory.getLogger(ServiceAddressSortingServiceImpl.class);

  @Inject
  private MetricsAccessor metricsAccessor;

  private final MetricSpec requestNextUnsortedMetricSpec =
      ImmutableMetricSpec
          .builder()
          .action("request_next_unsorted")
          .addLabels("user")
          .build();

  private final MetricSpec sortMetricSpec =
      ImmutableMetricSpec
          .builder()
          .action("sort")
          .addLabels("type", "user")
          .build();

  private final MetricSpec unsortMetricSpec =
      ImmutableMetricSpec
          .builder()
          .action("unsort")
          .addLabels("user")
          .build();

  private final MetricSpec skipMetricSpec =
      ImmutableMetricSpec
          .builder()
          .action("skip")
          .addLabels("user")
          .build();

  private final MetricSpec sortingImpossibleMetricSpec =
      ImmutableMetricSpec
          .builder()
          .action("sorting_impossible")
          .addLabels("user")
          .build();

  private final MetricSpec errorMetricSpec =
      ImmutableMetricSpec
          .builder()
          .action("error")
          .addLabels("type")
          .build();

  @Inject
  private SortableServiceAddressFetcher sortableServiceAddressFetcher;

  @Inject
  ServiceAddressBundleFetcher serviceAddressBundleFetcher;

  @Inject
  LawFirmRepository lawFirmRepository;

  @Inject
  ServiceAddressSorter serviceAddressSorter;

  @Override
  public void nextUnsortedServiceAddress(final NextUnsortedServiceAddressRequest request,
                                         final StreamObserver<ServiceAddressBundle> responseObserver) {
    try {
      final Optional<ServiceAddressBundle> serviceAddressBundle =
          sortableServiceAddressFetcher
              .fetchNext(request.getRequestedBy())
              .map(serviceAddressBundleFetcher::fetch);

      if (!serviceAddressBundle.isPresent()) {
        responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        return;
      }
      responseObserver.onNext(serviceAddressBundle.get());
      responseObserver.onCompleted();
      metricsAccessor.getCounter(requestNextUnsortedMetricSpec).inc(request.getRequestedBy());
    } catch (Throwable th) {
      log.error("Error generating next unsorted service address for request: " + request.toString(), th);
      responseObserver.onError(th);
      metricsAccessor.getCounter(errorMetricSpec).inc("next_unsorted_service_address");
    }
  }

  @Override
  public void searchLawFirms(final SearchLawFirmsRequest request, final StreamObserver<SearchResults> responseObserver) {
    if (request.getSearchTerm().isEmpty()) {
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Search term is required").asRuntimeException());
      return;
    }
    try {
      responseObserver.onNext(
          SearchResults
              .newBuilder()
              .addAllLawFirmAgents(lawFirmRepository.searchLawFirms(request.getSearchTerm(), request.getCountryCode()))
              .build()
      );
      responseObserver.onCompleted();
    } catch (Throwable th) {
      log.error("Error searching law firms for request: " + request.toString(), th);
      responseObserver.onError(th);
      metricsAccessor.getCounter(errorMetricSpec).inc("search_law_firms");
    }
  }

  @Override
  public void assignServiceAddress(final AssignServiceAddressRequest request,
                                   final StreamObserver<AssignServiceAddressResponse> responseObserver) {
    try {
      responseObserver.onNext(serviceAddressSorter.assignServiceAddress(request));
      responseObserver.onCompleted();
      metricsAccessor.getCounter(sortMetricSpec).inc("assign_to_law_firm", request.getRequestedBy());
    } catch (Throwable th) {
      log.error("Error assigning service address to law firm for request: " + request.toString(), th);
      responseObserver.onError(th);
      metricsAccessor.getCounter(errorMetricSpec).inc("assign_service_address");
    }
  }

  @Override
  public void unsortServiceAddress(final UnsortServiceAddressRequest request,
                                   final StreamObserver<UnsortServiceAddressResponse> responseObserver) {
    try {
      serviceAddressSorter.unsortServiceAddress(request);
      responseObserver.onNext(UnsortServiceAddressResponse.getDefaultInstance());
      responseObserver.onCompleted();
      metricsAccessor.getCounter(unsortMetricSpec).inc(request.getRequestedBy());
    } catch (Throwable th) {
      log.error("Error unsorting service address for request: " + request.toString(), th);
      responseObserver.onError(th);
      metricsAccessor.getCounter(errorMetricSpec).inc("unsort_service_address");
    }
  }

  @Override
  public void createLawFirm(final CreateLawFirmRequest request, final StreamObserver<CreateLawFirmResponse>
      responseObserver) {
    try {
      responseObserver.onNext(serviceAddressSorter.createLawFirmAndAssignServiceAddress(request));
      responseObserver.onCompleted();
      metricsAccessor.getCounter(sortMetricSpec).inc("create_law_firm", request.getRequestedBy());
    } catch (Throwable th) {
      log.error("Error creating new law firm using service address for request: " + request.toString(), th);
      responseObserver.onError(th);
      metricsAccessor.getCounter(errorMetricSpec).inc("create_law_firm");
    }
  }

  @Override
  public void setServiceAddressAsNonLawFirm(final SetServiceAddressAsNonLawFirmRequest request,
                                            final StreamObserver<SetServiceAddressAsNonLawFirmResponse> responseObserver) {
    try {
      responseObserver.onNext(serviceAddressSorter.setServiceAddressAsNonLawFirm(request));
      responseObserver.onCompleted();
      metricsAccessor.getCounter(sortMetricSpec).inc("set_non_law_firm", request.getRequestedBy());
    } catch (Throwable th) {
      log.error("Error setting service address as non law firm for request: " + request.toString(), th);
      responseObserver.onError(th);
      metricsAccessor.getCounter(errorMetricSpec).inc("set_service_address_as_non_law_firm");
    }
  }

  @Override
  public void skipServiceAddress(SkipServiceAddressRequest request,
                                 StreamObserver<SkipServiceAddressResponse> responseObserver) {
    // Simply log the fact and do nothing
    // Skipping is built into the get next unsorted service address endpoint of ip-data-relational
    responseObserver.onNext(SkipServiceAddressResponse.getDefaultInstance());
    responseObserver.onCompleted();
    metricsAccessor.getCounter(skipMetricSpec).inc(request.getRequestedBy());
  }

  @Override
  public void insufficientInfoToSort(final SetInsufficientInfoStatusRequest request,
                                     final StreamObserver<SetInsufficientInfoStatusResponse> responseObserver) {
    try {
      responseObserver.onNext(serviceAddressSorter.setInsufficientInfoToSort(request));
      responseObserver.onCompleted();
      metricsAccessor.getCounter(sortingImpossibleMetricSpec).inc(request.getRequestedBy());
    } catch (Throwable th) {
      log.error("Error setting insufficient info to sort request: " + request.toString(), th);
      responseObserver.onError(th);
      metricsAccessor.getCounter(errorMetricSpec).inc("insufficient_info_to_sort");
    }
  }
}
