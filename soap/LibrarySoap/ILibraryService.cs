using CoreWCF;

namespace LibrarySoap;

public static class Soap
{
    /// <summary>XML namespace for every element in our contract (operations, types, faults).</summary>
    public const string Namespace = "http://library.example/soap";
}

/// <summary>
/// The SOAP contract, i.e. what ends up in the WSDL.
/// Every [OperationContract] becomes a wsdl:operation; every [FaultContract] lists a fault it may return.
/// </summary>
[ServiceContract(Name = "LibraryService", Namespace = Soap.Namespace)]
public interface ILibraryService
{
    // ---- Books ----

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    int CreateBook(string title, int authorId, int publishingCompanyId, int publishingYear);

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    Book GetBookById(int id);

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    bool UpdateBook(int id, string title, int authorId, int publishingCompanyId, int publishingYear);

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    bool DeleteBook(int id);

    // ---- Authors ----

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    int CreateAuthor(string name, string surname);

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    Author GetAuthorById(int id);

    [OperationContract]
    Author[] ListAuthors();

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    bool UpdateAuthor(int id, string name, string surname);

    [OperationContract]
    [FaultContract(typeof(ValidationFault))]
    [FaultContract(typeof(NotFoundFault))]
    [FaultContract(typeof(ConflictFault))]
    bool DeleteAuthor(int id);

    // ---- Publishing companies ----

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
