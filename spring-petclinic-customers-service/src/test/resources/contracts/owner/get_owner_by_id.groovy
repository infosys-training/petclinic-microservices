import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return owner details by id"
    request {
        method GET()
        url "/owners/1"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            id       : 1,
            firstName: "George",
            lastName : "Franklin",
            address  : "110 W. Liberty St.",
            city     : "Madison",
            telephone: "6085551023",
            pets     : [
                [
                    id       : 1,
                    name     : "Leo",
                    birthDate: "2010-09-07",
                    type     : [
                        id  : 1,
                        name: "cat"
                    ]
                ]
            ]
        ])
        bodyMatchers {
            jsonPath('$.id', byType())
            jsonPath('$.firstName', byType())
            jsonPath('$.lastName', byType())
            jsonPath('$.address', byType())
            jsonPath('$.city', byType())
            jsonPath('$.telephone', byType())
            jsonPath('$.pets[0].id', byType())
            jsonPath('$.pets[0].name', byType())
            jsonPath('$.pets[0].birthDate', byType())
            jsonPath('$.pets[0].type.id', byType())
            jsonPath('$.pets[0].type.name', byType())
        }
    }
}
