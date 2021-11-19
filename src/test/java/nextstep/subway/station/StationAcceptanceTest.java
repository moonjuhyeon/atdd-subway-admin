package nextstep.subway.station;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.station.dto.StationRequest;
import nextstep.subway.station.dto.StationResponse;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {

	public static final StationRequest 강남역_생성_요청값 = new StationRequest("강남역");
	public static final StationRequest 역삼역_생성_요청값 = new StationRequest("역삼역");
	public static final StationRequest 광교역_생성_요청값 = new StationRequest("광교역");
	public static final StationRequest 성수역_생성_요청값 = new StationRequest("성수역");
	private static final String STATION_PATH = "/stations";

	public static ExtractableResponse<Response> 지하철역_생성_요청(StationRequest params) {
		return RestAssured.given().log().all()
			.body(params)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.post(STATION_PATH)
			.then().log().all()
			.extract();
	}

	@DisplayName("지하철역을 생성한다.")
	@Test
	void createStation() {
		// when
		ExtractableResponse<Response> response = 지하철역_생성_요청(강남역_생성_요청값);

		// then
		지하철역_생성됨(response);
	}

	void 지하철역_생성됨(ExtractableResponse<Response> response) {
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
			() -> assertThat(response.header("Location")).isNotBlank()
		);
	}

	@DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
	@Test
	void createStationWithDuplicateName() {
		// given
		지하철역_생성_요청(강남역_생성_요청값);

		// when
		ExtractableResponse<Response> response = 지하철역_생성_요청(강남역_생성_요청값);

		// then
		지하철역_생성_실패됨(response);
	}

	void 지하철역_생성_실패됨(ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@DisplayName("지하철역을 조회한다.")
	@Test
	void getStations() {
		/// given
		ExtractableResponse<Response> 강남역_생성됨 = 지하철역_생성_요청(강남역_생성_요청값);
		ExtractableResponse<Response> 역삼역_생성됨 = 지하철역_생성_요청(역삼역_생성_요청값);

		// when
		ExtractableResponse<Response> response = 지하철역_목록_조회();

		// then
		지하철역_목록_조회됨(response, 강남역_생성됨, 역삼역_생성됨);
	}

	ExtractableResponse<Response> 지하철역_목록_조회() {
		return RestAssured.given().log().all()
			.when()
			.get(STATION_PATH)
			.then().log().all()
			.extract();
	}

	void 지하철역_목록_조회됨(ExtractableResponse<Response> response, ExtractableResponse<Response> createResponse1,
		ExtractableResponse<Response> createResponse2) {
		List<Long> expectedLineIds = Arrays.asList(createResponse1, createResponse2).stream()
			.map(it -> Long.parseLong(it.header("Location").split("/")[2]))
			.collect(Collectors.toList());
		List<Long> resultLineIds = response.jsonPath().getList(".", StationResponse.class).stream()
			.map(it -> it.getId())
			.collect(Collectors.toList());

		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(resultLineIds).containsAll(expectedLineIds)
		);

	}

	@DisplayName("지하철역을 제거한다.")
	@Test
	void deleteStation() {
		// given
		ExtractableResponse<Response> createResponse = 지하철역_생성_요청(강남역_생성_요청값);

		// when
		String uri = createResponse.header("Location");
		ExtractableResponse<Response> response = 지하철역_제거(uri);

		// then
		지하철역_제거됨(response);
	}

	ExtractableResponse<Response> 지하철역_제거(String uri) {
		return RestAssured.given().log().all()
			.when()
			.delete(uri)
			.then().log().all()
			.extract();
	}

	void 지하철역_제거됨(ExtractableResponse<Response> response) {
		assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}
}
