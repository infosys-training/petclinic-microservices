import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return visits for given pet ids"
    request {
        method GET()
        url("/pets/visits") {
            queryParameters {
                parameter 'petId': '7'
            }
        }
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            items: [
                [
                    id         : 1,
                    petId      : 7,
                    date       : "2013-01-01",
                    description: "rabies shot"
                ],
                [
                    id         : 4,
                    petId      : 7,
                    date       : "2013-01-04",
                    description: "spayed"
                ]
            ]
        ])
        bodyMatchers {
            jsonPath('$.items', byType { minOccurrence(1) })
            jsonPath('$.items[0].id', byType())
            jsonPath('$.items[0].petId', byType())
            jsonPath('$.items[0].date', byType())
            jsonPath('$.items[0].description', byType())
        }
    }
}
