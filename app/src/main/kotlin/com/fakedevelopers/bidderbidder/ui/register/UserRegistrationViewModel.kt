package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.ACCEPT_TERMS
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_ID
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_PASSWORD
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_BEFORE_SENDING
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_CHECK_AUTH_CODE
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserRegistrationViewModel : ViewModel() {

    /* AcceptTermsFragment */
    private val _acceptAllState = MutableEventFlow<Boolean>()
    private val essentialTerms = Array(NUMBER_OF_ESSENTIAL_TERM) { false }
    private val optionalTerms = Array(NUMBER_OF_OPTIONAL_TERM) { false }
    val acceptAllState = _acceptAllState.asEventFlow()

    /* PhoneAuthFragment */
    private var phoneAuthToken = ""
    private val _checkAuthCode = MutableEventFlow<Boolean>()
    val checkAuthCode = _checkAuthCode.asEventFlow()
    private var _authCode = MutableStateFlow("")
    val authCode: StateFlow<String> get() = _authCode

    /* UserRegistrationBirthFragment */
    private var _birth = MutableStateFlow("")
    val birth: StateFlow<String> get() = _birth

    /* UserRegistrationIdFragment */
    private var userId = ""
    private var lastDuplicationState = true
    private val _userIdDuplicationState = MutableEventFlow<Boolean>()
    val userIdDuplicationState = _userIdDuplicationState.asEventFlow()
    val inputUserId = MutableStateFlow("")

    /* UserRegistrationPasswordFragment */
    private var userPassword = ""
    private val _userPasswordConditionLengthState = MutableStateFlow(false)
    private val _userPasswordConditionCharacterState = MutableStateFlow(false)
    private val _userPasswordConfirmState = MutableStateFlow(false)
    private val _userPasswordConfirmVisible = MutableEventFlow<Boolean>()
    val userPasswordConditionLengthState: StateFlow<Boolean> get() = _userPasswordConditionLengthState
    val userPasswordConditionCharacterState: StateFlow<Boolean> get() = _userPasswordConditionCharacterState
    val userPasswordConfirmState: StateFlow<Boolean> get() = _userPasswordConfirmState
    val userPasswordConfirmVisible = _userPasswordConfirmVisible.asEventFlow()
    val inputUserPassword = MutableStateFlow("")
    val inputConfirmUserPassword = MutableStateFlow("")

    // 현재 진행 상황
    private var currentStep = ACCEPT_TERMS
    private val _changeRegistrationStep = MutableEventFlow<RegistrationProgressState>()
    private val _nextStepEnabled = MutableStateFlow(false)
    val changeRegistrationStep = _changeRegistrationStep.asEventFlow()
    val nextStepEnabled: StateFlow<Boolean> get() = _nextStepEnabled

    // 실패 토스트 메세지
    private val _failureMessage = MutableEventFlow<String>()
    val failureMessage = _failureMessage.asEventFlow()

    /* AcceptTermsFragment */
    // 모든 약관 동의
    fun setAcceptAllState(isChecked: Boolean) {
        viewModelScope.launch {
            _acceptAllState.emit(isChecked)
        }
    }

    // 약관 선택 상태 변경
    fun setTermState(type: Int, idx: Int, isChecked: Boolean) {
        when (type) {
            TYPE_ESSENTIAL -> essentialTerms[idx] = isChecked
            TYPE_OPTIONAL -> optionalTerms[idx] = isChecked
        }
        checkNextStep(false)
    }

    // 필수 약관에 모두 동의 했다면 폰 인증 화면으로 넘어갑니다.
    private fun checkAcceptTerms(moveNextStep: Boolean = true) {
        if (essentialTerms.all { it }) {
            if (moveNextStep) {
                setCurrentStep(INPUT_ID)
            } else {
                setNextStepEnabled(true)
            }
        } else {
            if (moveNextStep) {
                sendFailureMessage(NOT_AGREE_TO_ESSENTIAL_TERMS)
            } else {
                setNextStepEnabled(false)
            }
        }
    }

    private fun checkNextAuthCode(moveNextStep: Boolean = true) {
        if (authCode.value.length == 6) {
            if (moveNextStep) {
                viewModelScope.launch { _checkAuthCode.emit(true) }
            } else {
                setNextStepEnabled(true)
            }
        } else {
            if (moveNextStep) {
                // unused
            } else {
                setNextStepEnabled(false)
            }
        }
    }

    fun setAuthCode(authCode: String) {
        viewModelScope.launch {
            _authCode.emit(authCode)
        }
        checkNextStep(false)
    }

    /* PhoneAuthFragment */
    // 휴대폰 인증 토큰 설정
    fun setPhoneAuthToken(token: String) {
        phoneAuthToken = token
    }

    /* UserRegistrationBirthFragment */
    // 생년월일 설정
    fun setBirth(birth: String) {
        viewModelScope.launch {
            _birth.emit(birth)
        }
        checkNextStep(false)
    }

    // 지금은 단순히 생년월일로 뭔갈 넣었다면
    private fun checkBirth(moveNextStep: Boolean = true) {
        if (birth.value.isNotEmpty()) {
            if (moveNextStep) {
                setCurrentStep(INPUT_ID)
            } else {
                setNextStepEnabled(true)
            }
        } else {
            if (moveNextStep) {
                sendFailureMessage(EMPTY_BIRTH)
            } else {
                setNextStepEnabled(false)
            }
        }
    }

    /* UserRegistrationIdFragment */
    // 아이디 중복 검사
    fun isUserIdDuplicated() {
        // 비어 있다면 검사는 필요 없어
        if (inputUserId.value.isEmpty()) {
            setUserIdDuplicationState(true)
            return
        }
        // 여기서는 api를 호출해서 inputId에 중복이 있는지 확인 합니다.
        // 물론 아직 그런건 없으므로 테스트용 EXIST_ID와 같은지만 비교해봅니다.
        lastDuplicationState = inputUserId.value == EXIST_ID
        if (!lastDuplicationState) {
            // 중복이 아니라면 입력칸의 id를 userId로 설정합니다.
            userId = inputUserId.value
        }
        setUserIdDuplicationState(lastDuplicationState)
    }

    fun getIdDuplicationState() = lastDuplicationState

    // 이미 중복 체크된 아이디거나 중복 체크를 통과한 아이디면 다음 단계로 갑니다.
    private fun checkUserId(moveNextStep: Boolean = true) {
        if (!lastDuplicationState && userId == inputUserId.value) {
            if (moveNextStep) {
                setCurrentStep(INPUT_PASSWORD)
            } else {
                setNextStepEnabled(true)
            }
        } else {
            if (moveNextStep) {
                sendFailureMessage(NOT_ID_DUPLICATION_CHECK)
            } else {
                setNextStepEnabled(false)
            }
        }
    }

    // 아이디 중복 여부 표시
    private fun setUserIdDuplicationState(state: Boolean) {
        viewModelScope.launch {
            _userIdDuplicationState.emit(state)
        }
        checkNextStep(false)
    }

    /* UserRegistrationIdFragment */
    // 비밀번호 조건 검사
    fun checkPasswordCondition() {
        val lengthCondition = inputUserPassword.value.length in PASSWORD_LENGTH_MINIMUM..PASSWORD_LENGTH_MAXIMUM
        val characterCondition = PASSWORD_CHARACTER_CONDITION.matchEntire(inputUserPassword.value) != null
        viewModelScope.launch {
            _userPasswordConditionLengthState.emit(lengthCondition)
            _userPasswordConditionCharacterState.emit(characterCondition)
            _userPasswordConfirmVisible.emit(lengthCondition && characterCondition)
        }
        if (lengthCondition && characterCondition) {
            checkPasswordConfirm()
        }
    }

    // 비밀번호 확인
    fun checkPasswordConfirm() {
        viewModelScope.launch {
            _userPasswordConfirmState.emit(inputConfirmUserPassword.value == inputUserPassword.value)
        }
        checkNextStep(false)
    }

    // 비밀번호 검증 (마지막 단계)
    private fun checkUserPassword(moveNextStep: Boolean = true) {
        if (
            userPasswordConditionCharacterState.value &&
            userPasswordConditionLengthState.value &&
            userPasswordConfirmState.value
        ) {
            userPassword = inputUserPassword.value
            // 가입 완료 판정을 받기 전에 지금까지 모은 정보를 서버에 보내는 작업이 필요합니다.
            // 서버가 ok와 함께 토큰을 던져주고 그걸 저장까지 했을 때 다음 화면으로 넘어갑니다.
            if (moveNextStep) {
                setCurrentStep(PHONE_AUTH_BEFORE_SENDING)
            } else {
                setNextStepEnabled(true)
            }
        } else {
            if (moveNextStep) {
                sendFailureMessage(INVALID_REGEX)
            } else {
                setNextStepEnabled(false)
            }
        }
    }

    // 다음 단계 버튼 활성화
    fun setNextStepEnabled(state: Boolean) {
        viewModelScope.launch {
            _nextStepEnabled.emit(state)
        }
    }

    // 다음 단계를 가기 전 검증
    fun checkNextStep(moveNextStep: Boolean = true) {
        when (currentStep) {
            ACCEPT_TERMS -> checkAcceptTerms(moveNextStep)
            INPUT_ID -> checkUserId(moveNextStep)
            INPUT_PASSWORD -> checkUserPassword(moveNextStep)
            PHONE_AUTH_BEFORE_SENDING -> sendFailureMessage(NOT_RECEIVED_AUTH_CODE)
            PHONE_AUTH_CHECK_AUTH_CODE -> checkNextAuthCode(moveNextStep)
            else -> {
                // 여긴 갈 일 없어!
            }
        }
    }

    fun getCurrentStep() = currentStep

    // 단계 설정
    fun setCurrentStep(step: RegistrationProgressState) {
        currentStep = step
        viewModelScope.launch {
            _changeRegistrationStep.emit(step)
        }
    }

    // 이전 단계로 돌아가자
    fun toPreviousStep() {
        when (currentStep) {
            INPUT_PASSWORD -> setCurrentStep(INPUT_ID)
            PHONE_AUTH_BEFORE_SENDING -> setCurrentStep(INPUT_PASSWORD)
            PHONE_AUTH_CHECK_AUTH_CODE -> setCurrentStep(INPUT_PASSWORD)
            else -> sendFailureMessage(NOT_GO_BACKWARDS)
        }
    }

    // 실패 토스트 메세지
    private fun sendFailureMessage(msg: String) {
        viewModelScope.launch {
            _failureMessage.emit(msg)
        }
    }

    companion object {
        const val NUMBER_OF_ESSENTIAL_TERM = 4
        const val NUMBER_OF_OPTIONAL_TERM = 1

        // 약관 타입
        const val TYPE_ESSENTIAL = 0
        const val TYPE_OPTIONAL = 1

        // 비밀번호 조건
        const val PASSWORD_LENGTH_MINIMUM = 12
        const val PASSWORD_LENGTH_MAXIMUM = 24

        // 허용되는 특수문자 : !@#$%^+-=
        // 나중에 명확히 정해지면 그걸루 가도 되겠죠
        val PASSWORD_CHARACTER_CONDITION = Regex("^(?=.*[A-Za-z])(?=.*[0-9])[a-zA-Z0-9!@#\$%^+\\-=]*$")

        // 실패 메세지
        const val NOT_GO_BACKWARDS = "가지마!!"
        const val NOT_AGREE_TO_ESSENTIAL_TERMS = "필수 약관에 모두 동의 해!!"
        const val NOT_RECEIVED_AUTH_CODE = "인증번호를 입력해!!"
        const val EMPTY_BIRTH = "생년월일이 없잖아!!"
        const val NOT_ID_DUPLICATION_CHECK = "이게 아이디야?!"

        // 소나가 뭐라캐서 이름을 고칩니다.
        const val INVALID_REGEX = "이게 비밀번호야?!"

        // 테스트용 중복 ID
        const val EXIST_ID = "bidder123"
    }
}
