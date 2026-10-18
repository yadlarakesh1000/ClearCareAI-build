import json
from pathlib import Path
import pytest
from schemas import OmniDimWebhookPayload
FIXTURE_PATH =(
   Path(__file__).parent
   / "fixtures"
   / "omnidim_call_completed.json"
)
@pytest.fixture
def omnidim_payload_json():
    with FIXTURE_PATH.open("r", encoding="utf-8") as file:
        return json.load(file)


@pytest.fixture
def omnidim_payload(omnidim_payload_json):
    return OmniDimWebhookPayload.model_validate(
        omnidim_payload_json
    )