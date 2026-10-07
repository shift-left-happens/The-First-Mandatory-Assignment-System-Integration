using CoreWCF;
using LibrarySoap.Shared;

namespace LibrarySoap;

/// <summary>
/// The SOAP contract, i.e. what ends up in the WSDL.
/// It is a <c>partial</c> interface: each feature folder adds its own operations
/// (see Features/*/ILibraryService.*.cs), but it is still ONE contract, ONE endpoint and ONE WSDL.
/// </summary>
[ServiceContract(Name = "LibraryService", Namespace = Soap.Namespace)]
public partial interface ILibraryService
{
}
