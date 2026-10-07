using CoreWCF;
using LibrarySoap.Features.PublishingCompanies;
using LibrarySoap.Shared;

namespace LibrarySoap;

public partial interface ILibraryService
{
    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    int CreatePublishingCompany(string name);

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    PublishingCompany GetPublishingCompanyById(int id);

    [OperationContract]
    PublishingCompany[] ListPublishingCompanies();

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    bool UpdatePublishingCompany(int id, string name);

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    [FaultContract(typeof(ConflictFault))]
    bool DeletePublishingCompany(int id);
}
