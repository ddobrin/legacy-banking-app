export type AccountStatus = 'ACTIVE' | 'FROZEN' | 'CLOSED' | 'OPEN';

export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'WITHDRAW' | 'TRANSFER';

export interface ProductDto {
  productCode: string;
  name: string;
  category: string;
  interestRate: number | null;
  active: boolean;
}

export interface AccountDto {
  accountId: number;
  customerId: number;
  productCode: string;
  accountNumber: string;
  iban?: string | null;
  balance: number;
  overdraftLimit?: number | null;
  status: AccountStatus | string;
  openedAt?: string | null;
  closedAt?: string | null;
  configurations?: string | null;
}

export interface CustomerDto {
  customerId: number;
  cifNumber: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  email: string;
  phoneNumber?: string | null;
  addressLine1?: string | null;
  city?: string | null;
  state?: string | null;
  postalCode?: string | null;
  countryCode?: string | null;
  kycStatus?: string | null;
  kycDocuments?: string | null;
  riskRating?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  accounts?: AccountDto[] | null;
  accountCount?: number | null;
}

export interface TransactionDto {
  transactionId: number;
  accountId: number;
  referenceCode?: string | null;
  transactionType: TransactionType | string;
  amount: number;
  description?: string | null;
  transactionDate: string;
  balanceAfter?: number | null;
}

export interface CreateCustomerRequest {
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth: string;
  cifNumber?: string;
  phoneNumber?: string;
  addressLine1?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  countryCode?: string;
  riskRating?: string;
}

export interface UpdateCustomerRequest {
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth: string;
  phoneNumber?: string;
  addressLine1?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  countryCode?: string;
  kycStatus?: string;
  riskRating?: string;
}

export interface CreateAccountRequest {
  customerId: number;
  productCode: string;
  balance?: number;
  overdraftLimit?: number;
  iban?: string;
}

export interface UpdateAccountStatusRequest {
  status: 'ACTIVE' | 'FROZEN' | 'CLOSED';
}

export interface CreateTransactionRequest {
  accountId: number;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
  amount: number;
  description?: string;
  referenceCode?: string;
  targetAccountId?: number;
}

export interface ApiErrorResponse {
  status?: number;
  error?: string;
  message?: string;
  timestamp?: string;
}
