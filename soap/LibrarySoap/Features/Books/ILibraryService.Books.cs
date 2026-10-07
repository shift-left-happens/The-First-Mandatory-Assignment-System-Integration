using CoreWCF;
using LibrarySoap.Features.Books;
using LibrarySoap.Shared;

namespace LibrarySoap;

public partial interface ILibraryService
{
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
}
