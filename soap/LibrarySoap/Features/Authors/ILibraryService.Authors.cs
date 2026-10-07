using CoreWCF;
using LibrarySoap.Features.Authors;
using LibrarySoap.Shared;

namespace LibrarySoap;

public partial interface ILibraryService
{
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
}
